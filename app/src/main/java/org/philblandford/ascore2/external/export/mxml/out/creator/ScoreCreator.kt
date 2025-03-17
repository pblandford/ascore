package org.philblandford.ascore2.external.export.mxml.out.creator

import com.philblandford.ascore.external.export.mxml.out.creator.createScorePart
import com.philblandford.ascore.external.export.mxml.out.creator.repeatBarQuery
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.MetaType
import com.philblandford.kscore.engine.types.eZero
import org.philblandford.ascore2.external.export.mxml.out.MxmlCreator
import org.philblandford.ascore2.external.export.mxml.out.MxmlCredit
import org.philblandford.ascore2.external.export.mxml.out.MxmlCreditWords
import org.philblandford.ascore2.external.export.mxml.out.MxmlIdentification
import org.philblandford.ascore2.external.export.mxml.out.MxmlPartList
import org.philblandford.ascore2.external.export.mxml.out.MxmlScorePartwise
import org.philblandford.ascore2.external.export.mxml.out.MxmlWork
import org.philblandford.ascore2.external.export.mxml.out.MxmlWorkTitle
import org.philblandford.ascore2.external.export.mxml.out.getAlignment

internal fun createMxmlScore(score: Score): MxmlScorePartwise? {
    val work = createWork(score)
    val identification = score.createIdentification()
    val defaults = score.createDefaults()
    val credits = score.createCredits()
    val partList = createPartList(score)
    val repeatBarQuery = repeatBarQuery(score)
    val parts = score.allParts(true).mapNotNull { partNum ->
        score.getPart(partNum)?.let { part ->
            createPart(
                part, partList.scoreParts.find { it.partNum == partNum }!!, partNum, score,
                repeatBarQuery
            )
        }
    }
    return MxmlScorePartwise(
        work = work,
        identification = identification,
        parts = parts,
        partList = partList,
        defaults = defaults,
        credits = credits
    )
}

private fun createPartList(score: Score): MxmlPartList {
    val scoreParts = score.allParts(true).mapNotNull { partNum ->
        score.getPart(partNum)?.let { part ->
            createScorePart(part, partNum)
        }
    }
    return MxmlPartList(scoreParts)
}

private fun createWork(score: Score): MxmlWork? {
    return score.getTitle()?.let { title ->
        MxmlWork(MxmlWorkTitle(title))
    }
}

private fun Score.createIdentification(): MxmlIdentification? {
    val composer = getMeta(MetaType.COMPOSER)?.let { c ->
        MxmlCreator("composer", c)
    }
    val lyricist = getMeta(MetaType.LYRICIST)?.let { l ->
        MxmlCreator("lyricist", l)
    }
    val creators = listOfNotNull(composer, lyricist)
    return if (creators.isNotEmpty()) {
        MxmlIdentification(creators)
    } else null
}

private fun Score.getMeta(metaType: MetaType): String? {
    return getParam<String>(metaType.toEventType(), EventParam.TEXT, eZero())
}

private fun Score.createCredits(): List<MxmlCredit> {
    return MetaType.values().mapNotNull { metaType ->
        val text = getParam<String>(metaType.toEventType(), EventParam.TEXT) ?: ""
        if (text.isNotEmpty()) {
            getAlignment(metaType)?.let { alignment ->
                val words = MxmlCreditWords(alignment.justify, alignment.valign, text)
                MxmlCredit(words)
            }
        } else null
    }
}
