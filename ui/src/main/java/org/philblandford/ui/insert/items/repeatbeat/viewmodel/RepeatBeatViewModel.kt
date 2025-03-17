package org.philblandford.ui.insert.items.repeatbeat.viewmodel

import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.items.repeatbeat.model.RepeatBeatModel
import org.philblandford.ui.insert.model.InsertInterface

interface RepeatBeatInterface : InsertInterface<RepeatBeatModel> {
    fun setDuration(duration: Duration)
}

class RepeatBeatViewModel : InsertViewModel<RepeatBeatModel, RepeatBeatInterface>(),
    RepeatBeatInterface {
    override fun setDuration(duration: Duration) {
        update { copy(duration = duration) }
        setParam(EventParam.DURATION, duration)
    }

    override fun getInterface(): RepeatBeatInterface {
        return this
    }

    override suspend fun initState(): Result<RepeatBeatModel> {
        return RepeatBeatModel(crotchet()).ok().onSuccess {
            setParam(EventParam.DURATION, it.duration)
        }
    }
}