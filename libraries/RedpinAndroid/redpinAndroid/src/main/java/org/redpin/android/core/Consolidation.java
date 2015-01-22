package org.redpin.android.core;

import org.redpin.android.db.RemoteEntity;

/**
 * Created by louismagarshack on 11/19/14.
 */
public class Consolidation extends org.redpin.base.core.Consolidation
implements RemoteEntity<Integer> {

    protected Integer id;

    public Consolidation(Fingerprint fp, Measurement meas) {
        super(fp, meas);
    }

    public Integer getRemoteId() {
        return id;
    }

    public void setRemoteId(Integer id) {
        this.id = id;
    }


}
