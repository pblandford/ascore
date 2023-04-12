package com.philblandford.kscore.select;

import com.philblandford.kscore.engine.types.EventAddress;

public interface SelectionQuery {
    EventAddress getStartSelection();
    EventAddress getEndSelection();
    EventAddress getPlaybackMarker();
    AreaToShow getAreaToShow();
    int getAreaBar();
}
