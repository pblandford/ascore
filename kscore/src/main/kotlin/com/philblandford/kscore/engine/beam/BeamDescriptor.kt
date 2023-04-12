import com.philblandford.kscore.engine.beam.Beam
import com.philblandford.kscore.engine.beam.BeamMap
import com.philblandford.kscore.engine.duration.*

enum class BeamPos{ START, MID, END}
data class BeamDescriptor(val duration: Duration, val members:Iterable<Offset>) {

 val start = members.first()
  val end = members.last()
}
data class BeamState(val duration: Duration, val beamPos: BeamPos)

fun getBeamDescriptors(beam: Beam): List<BeamDescriptor> {

  val map = mutableMapOf<Offset, List<Duration>>()

  var offset = dZero()

  beam.members.forEach { member ->

    val durations = (1..quaver().divide(member.duration.undot()).toInt()).map { div ->
      quaver().divide(div)
    }
    map[offset] = durations
    offset = offset.addC(member.realDuration)
  }

  val min = beam.members.minByOrNull { it.duration }?.duration ?: quaver()

  return (1..quaver().divide(min.undot()).toInt()).flatMap { div ->
    val duration = Duration(1, (4 shl div))
    val offsets = map.filter { it.value.contains(duration) }.keys.sorted()
    getDescriptorsFromOffsets(duration, offsets, map.keys)
  }
}

private fun getDescriptorsFromOffsets(
  duration: Duration,
  offsets: Iterable<Offset>,
  allOffsets: Iterable<Offset>
): Iterable<BeamDescriptor> {

  val descriptors = mutableListOf<BeamDescriptor>()
  var current: BeamDescriptor? = null

  allOffsets.forEach { offset ->

    if (!offsets.contains(offset)) {
      current?.let { descriptors.add(it) }
      current = null
    } else {
      if (current == null) {
        current = BeamDescriptor(duration, listOf(offset))
      } else {
        val members = current?.members ?: listOf()
        current = current?.copy(members = members.plus(offset))
      }
    }
  }
  current?.let { descriptors.add(it) }

  return descriptors
}

interface BeamStateQuery {
  fun getState(offset: Offset):Iterable<BeamState>
}

private class BeamStateQueryImpl(val map:Map<Offset, Iterable<BeamState>>)  : BeamStateQuery {
  override fun getState(offset: Offset): Iterable<BeamState> {
    return map[offset] ?: listOf()
  }
}

fun beamStateQuery(beamMap: BeamMap):BeamStateQuery {
  val map = mutableMapOf<Offset, Iterable<BeamState>>()

  beamMap.forEach { (k, beam) ->
    getBeamDescriptors(beam).forEach { desc ->
      desc.members.forEach { member ->
        val beamPos = if (member == desc.start) {
          BeamPos.START
        } else if (member == desc.end) {
          BeamPos.END
        } else {
          BeamPos.MID
        }
        val beamState = BeamState(desc.duration, beamPos)
        val offset = k.offset + member
        val list = map[offset] ?: listOf()
        map[offset] = list.plus(beamState)
      }
    }
  }
  return BeamStateQueryImpl(map)
}