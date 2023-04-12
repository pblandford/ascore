package com.philblandford.kscore.engine.dsl

import com.philblandford.kscore.api.defaultInstrumentGrand
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.duration.quaver
import com.philblandford.kscore.engine.types.Accidental.SHARP
import com.philblandford.kscore.engine.types.NoteLetter.*

fun blackSheep(): Score {
  return score(-1) {
    part {
      stave {
        bar {
          voiceMap {
            note(F)
            note(F)
            note(C, 5)
            note(C, 5)
          }
        }
        bar {
          voiceMap {
            toQuaver()
            note(D, 5)
            note(E, 5)
            note(F, 5)
            note(D, 5)
            note(C, 5, duration = minim())
          }
        }
        bar {
          voiceMap {
            toCrotchet()
            note(B)
            note(B)
            note(A)
            note(A)
          }
        }
        bar {
          voiceMap {
            note(G)
            note(G)
            note(F, duration = minim())
          }
        }
        bar {
          voiceMap {
            note(C, 5)
            note(C, 5, duration = quaver())
            note(C, 5, duration = quaver())
            note(B)
            note(B, duration = quaver())
            note(B, duration = quaver())
          }
        }
        bar {
          voiceMap {
            note(A)
            note(A, duration = quaver())
            note(A, duration = quaver())
            note(G, duration = crotchet(1))
            note(G, duration = quaver())
          }
        }
        bar {
          voiceMap {
            note(C, 5)
            note(C, 5, duration = quaver())
            note(C, 5, duration = quaver())
            note(B, duration = quaver())
            note(C, 5, duration = quaver())
            note(D, 5, duration = quaver())
            note(B, duration = quaver())
          }
        }
        bar {
          voiceMap {
            note(A)
            note(G, duration = quaver())
            note(G, duration = quaver())
            note(F, duration = minim())
          }
        }
      }
    }
  }
}

fun simpleQuavers(): Score {
  return score {
    part {
      stave {
        bar {
          voiceMap {
            note(C, 5, quaver())
            note(D, 5, quaver())
            note(E, 5, quaver())
            note(F, 5, quaver())
            note(C, 5, crotchet())
          }
        }
      }
    }
  }
}

fun dotted(): Score {
  return score {
    part {
      stave {
        bar {
          voiceMap {
            note(F, 4, crotchet(1))
            note(F, 4, quaver())
          }
        }
      }
    }
  }
}

fun createScoreOneNote(): Score {
  return score {
    part {
      stave {
        bar {
          voiceMap {
            chord(crotchet()) {
              pitch(F)
            }
          }
        }
      }
    }
  }
}

fun createScoreTwoNotes(): Score {
  return score {
    part {
      stave {
        bar {
          voiceMap {
            chord { pitch(F) }
            chord { pitch(F) }
          }
        }
      }
    }
  }
}

fun scoreAccidental(): Score {
  return score(1) {
    part {
      stave {
        bar {
          voiceMap {
            note(G, accidental = SHARP, showAccidental = true)
          }
        }
      }
    }
  }
}


fun scoreBar2(): Score {
  return score {
    part {
      stave {
        bar { voiceMap { } }
        bar {
          voiceMap {
            chord(crotchet()) {
              pitch(F)
            }
          }
        }
      }
    }
  }
}

fun scoreQuavers(): Score {
  return score {
    part {
      stave {
        bar {
          voiceMap {
            (1..8).forEach {
              chord(quaver()) {
                pitch(F)
              }
            }
          }
        }
      }
    }
  }
}

fun scoreAllCrotchets(numBars: Int): Score {
  return score {
    part {
      stave {
        (1..numBars).map {
          bar {
            voiceMap {
              chord(crotchet())
              chord(crotchet())
              chord(crotchet())
              chord(crotchet())
            }
          }
        }
      }
    }
  }
}

fun scoreGrandStave(numBars: Int): Score {
  return score {
    part(defaultInstrumentGrand()) {
      stave {
        (1..numBars).map {
          bar {
            voiceMap {
              (1..4).map {
                chord(crotchet())
              }
            }
          }
        }
      }
      stave {
        (1..numBars).map {
          bar {
            voiceMap {
              (1..4).map {
                chord(crotchet())
              }
            }
          }
        }
      }
    }
  }
}

fun scoreMultiInstruments(numParts: Int, numBars: Int): Score {
  return score {
    (1..numParts).map {
      part {
        stave {
          (1..numBars).map {
            bar {}
          }
        }
      }
    }
  }
}
