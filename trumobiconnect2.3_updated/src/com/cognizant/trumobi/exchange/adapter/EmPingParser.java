

package com.cognizant.trumobi.exchange.adapter;

import com.cognizant.trumobi.exchange.EmEasSyncService;
import com.cognizant.trumobi.exchange.EmIllegalHeartbeatException;
import com.cognizant.trumobi.exchange.EmStaleFolderListException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Parse the result of a Ping command.
 *
 * If there are folders with changes, add the serverId of those folders to the syncList array.
 * If the folder list needs to be reloaded, throw a StaleFolderListException, which will be caught
 * by the sync server, which will sync the updated folder list.
 */
public class EmPingParser extends EmParser {
    private ArrayList<String> syncList = new ArrayList<String>();
    private EmEasSyncService mService;
    private int mSyncStatus = 0;

    public ArrayList<String> getSyncList() {
        return syncList;
    }

    public int getSyncStatus() {
        return mSyncStatus;
    }

    public EmPingParser(InputStream in, EmEasSyncService service) throws IOException {
        super(in);
        mService = service;
    }

    public void parsePingFolders(ArrayList<String> syncList) throws IOException {
        while (nextTag(EmTags.PING_FOLDERS) != END) {
            if (tag == EmTags.PING_FOLDER) {
                // Here we'll keep track of which mailboxes need syncing
                String serverId = getValue();
                syncList.add(serverId);
                mService.userLog("Changes found in: ", serverId);
            } else {
                skipTag();
            }
        }
    }

    @Override
    public boolean parse() throws IOException, EmStaleFolderListException, EmIllegalHeartbeatException {
        boolean res = false;
        if (nextTag(START_DOCUMENT) != EmTags.PING_PING) {
            throw new IOException();
        }
        while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
            if (tag == EmTags.PING_STATUS) {
                int status = getValueInt();
                mSyncStatus = status;
                mService.userLog("Ping completed, status = ", status);
                if (status == 2) {
                    res = true;
                } else if (status == 7 || status == 4) {
                    // Status of 7 or 4 indicate a stale folder list
                    throw new EmStaleFolderListException();
                } else if (status == 5) {
                    // Status 5 means our heartbeat is beyond allowable limits
                    // In this case, there will be a heartbeat interval set
                }
            } else if (tag == EmTags.PING_FOLDERS) {
                parsePingFolders(syncList);
            } else if (tag == EmTags.PING_HEARTBEAT_INTERVAL) {
                // Throw an exception, saving away the legal heartbeat interval specified
                throw new EmIllegalHeartbeatException(getValueInt());
            } else {
                skipTag();
            }
        }
        return res;
    }
}

