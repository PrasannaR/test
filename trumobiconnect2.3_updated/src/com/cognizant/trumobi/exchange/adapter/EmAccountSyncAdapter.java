package com.cognizant.trumobi.exchange.adapter;

import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.exchange.EmEasSyncService;

import java.io.IOException;
import java.io.InputStream;

public class EmAccountSyncAdapter extends EmAbstractSyncAdapter {

    public EmAccountSyncAdapter(Mailbox mailbox, EmEasSyncService service) {
        super(mailbox, service);
     }

    @Override
    public void cleanup() {
    }

    @Override
    public String getCollectionName() {
        return null;
    }

    @Override
    public boolean parse(InputStream is) throws IOException {
        return false;
    }

    @Override
    public boolean sendLocalChanges(EmSerializer s) throws IOException {
        return false;
    }

    @Override
    public boolean isSyncable() {
        return true;
    }
}
