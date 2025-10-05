/*
 * Copyright (C) 2014 Michell Bak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.leopardmediahd.filesources;

import android.content.Context;
import android.database.Cursor;

import com.android.leopardmediahd.leopardmediahd.MizuuApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class SmbTvShow extends com.android.leopardmediahd.abstractclasses.TvShowFileSource<SmbFile> {

    private HashMap<String, String> existingEpisodes = new HashMap<String, String>();
    private SmbFile tempSmbFile;

    public SmbTvShow(Context context, com.android.leopardmediahd.functions.FileSource fileSource, boolean subFolderSearch, boolean clearLibrary, boolean disableEthernetWiFiCheck) {
        super(context, fileSource, subFolderSearch, clearLibrary, disableEthernetWiFiCheck);
    }

    @Override
    public void removeUnidentifiedFiles() {
        com.android.leopardmediahd.db.DbAdapterTvShowEpisodes db = com.android.leopardmediahd.leopardmediahd.MizuuApplication.getTvEpisodeDbAdapter();
        List<com.android.leopardmediahd.functions.DbEpisode> dbEpisodes = getDbEpisodes();

        ArrayList<com.android.leopardmediahd.functions.FileSource> filesources = com.android.leopardmediahd.functions.MizLib.getFileSources(com.android.leopardmediahd.functions.MizLib.TYPE_SHOWS, true);

        com.android.leopardmediahd.functions.FileSource source;
        SmbFile tempFile;
        int count = dbEpisodes.size();
        if (com.android.leopardmediahd.functions.MizLib.isWifiConnected(getContext())) {
            for (int i = 0; i < count; i++) {
                if (dbEpisodes.get(i).isUnidentified()) {
                    source = null;

                    for (int j = 0; j < filesources.size(); j++) {
                        if (dbEpisodes.get(i).getFilepath().contains(filesources.get(j).getFilepath())) {
                            source = filesources.get(j);
                            break;
                        }
                    }

                    if (source != null) {
                        try {
                            tempFile = new SmbFile(
                                    com.android.leopardmediahd.functions.MizLib.createSmbLoginString(
                                            source.getDomain(),
                                            source.getUser(),
                                            source.getPassword(),
                                            dbEpisodes.get(i).getFilepath(),
                                            false
                                    ));

                            if (tempFile.exists())
                                db.deleteEpisode(dbEpisodes.get(i).getShowId(), com.android.leopardmediahd.functions.MizLib.getInteger(dbEpisodes.get(i).getSeason()), com.android.leopardmediahd.functions.MizLib.getInteger(dbEpisodes.get(i).getEpisode()));

                        } catch (Exception e) {}
                    }
                }
            }
        }
    }

    @Override
    public void removeUnavailableFiles() {
        ArrayList<com.android.leopardmediahd.functions.DbEpisode> dbEpisodes = new ArrayList<com.android.leopardmediahd.functions.DbEpisode>(), removedEpisodes = new ArrayList<com.android.leopardmediahd.functions.DbEpisode>();

        // Fetch all the episodes from the database
        com.android.leopardmediahd.db.DbAdapterTvShowEpisodes db = com.android.leopardmediahd.leopardmediahd.MizuuApplication.getTvEpisodeDbAdapter();

        com.android.leopardmediahd.functions.ColumnIndexCache cache = new com.android.leopardmediahd.functions.ColumnIndexCache();
        Cursor tempCursor = db.getAllEpisodes();
        if (tempCursor == null)
            return;

        try {
            while (tempCursor.moveToNext())
                dbEpisodes.add(new com.android.leopardmediahd.functions.DbEpisode(getContext(),
                                com.android.leopardmediahd.leopardmediahd.MizuuApplication.getTvShowEpisodeMappingsDbAdapter().getFirstFilepath(tempCursor.getString(cache.getColumnIndex(tempCursor, com.android.leopardmediahd.db.DbAdapterTvShowEpisodes.KEY_SHOW_ID)), tempCursor.getString(cache.getColumnIndex(tempCursor, com.android.leopardmediahd.db.DbAdapterTvShowEpisodes.KEY_SEASON)), tempCursor.getString(cache.getColumnIndex(tempCursor, com.android.leopardmediahd.db.DbAdapterTvShowEpisodes.KEY_EPISODE))),
                                tempCursor.getString(cache.getColumnIndex(tempCursor, com.android.leopardmediahd.db.DbAdapterTvShowEpisodes.KEY_SHOW_ID)),
                                tempCursor.getString(cache.getColumnIndex(tempCursor, com.android.leopardmediahd.db.DbAdapterTvShowEpisodes.KEY_SEASON)),
                                tempCursor.getString(cache.getColumnIndex(tempCursor, com.android.leopardmediahd.db.DbAdapterTvShowEpisodes.KEY_EPISODE))
                        )
                );
        } catch (NullPointerException e) {
        } finally {
            tempCursor.close();
            cache.clear();
        }

        ArrayList<com.android.leopardmediahd.functions.FileSource> filesources = com.android.leopardmediahd.functions.MizLib.getFileSources(com.android.leopardmediahd.functions.MizLib.TYPE_SHOWS, true);

        SmbFile tempFile;
        int count = dbEpisodes.size();
        if (com.android.leopardmediahd.functions.MizLib.isWifiConnected(getContext())) {
            for (int i = 0; i < dbEpisodes.size(); i++) {
                com.android.leopardmediahd.functions.FileSource source = null;

                for (int j = 0; j < filesources.size(); j++)
                    if (dbEpisodes.get(i).getFilepath().contains(filesources.get(j).getFilepath())) {
                        source = filesources.get(j);
                        break;
                    }

                if (source == null) {
                    boolean deleted = db.deleteEpisode(dbEpisodes.get(i).getShowId(), com.android.leopardmediahd.functions.MizLib.getInteger(dbEpisodes.get(i).getSeason()), com.android.leopardmediahd.functions.MizLib.getInteger(dbEpisodes.get(i).getEpisode()));
                    if (deleted)
                        removedEpisodes.add(dbEpisodes.get(i));
                    continue;
                }

                try {
                    tempFile = new SmbFile(
                            com.android.leopardmediahd.functions.MizLib.createSmbLoginString(
                                    source.getDomain(),
                                    source.getUser(),
                                    source.getPassword(),
                                    dbEpisodes.get(i).getFilepath(),
                                    false
                            ));

                    if (!tempFile.exists()) {
                        boolean deleted = db.deleteEpisode(dbEpisodes.get(i).getShowId(), com.android.leopardmediahd.functions.MizLib.getInteger(dbEpisodes.get(i).getSeason()), com.android.leopardmediahd.functions.MizLib.getInteger(dbEpisodes.get(i).getEpisode()));
                        if (deleted)
                            removedEpisodes.add(dbEpisodes.get(i));
                    }
                } catch (Exception e) {
                    boolean deleted = db.deleteEpisode(dbEpisodes.get(i).getShowId(), com.android.leopardmediahd.functions.MizLib.getInteger(dbEpisodes.get(i).getSeason()), com.android.leopardmediahd.functions.MizLib.getInteger(dbEpisodes.get(i).getEpisode()));
                    if (deleted)
                        removedEpisodes.add(dbEpisodes.get(i));
                }
            }
        }

        count = removedEpisodes.size();
        for (int i = 0; i < count; i++) {
            if (db.getEpisodeCount(removedEpisodes.get(i).getShowId()) == 0) { // No more episodes for this show
                com.android.leopardmediahd.db.DbAdapterTvShows dbShow = com.android.leopardmediahd.leopardmediahd.MizuuApplication.getTvDbAdapter();
                boolean deleted = dbShow.deleteShow(removedEpisodes.get(i).getShowId());

                if (deleted) {
                    com.android.leopardmediahd.functions.MizLib.deleteFile(new File(removedEpisodes.get(i).getThumbnail()));
                    com.android.leopardmediahd.functions.MizLib.deleteFile(new File(removedEpisodes.get(i).getBackdrop()));
                }
            }

            com.android.leopardmediahd.functions.MizLib.deleteFile(new File(removedEpisodes.get(i).getEpisodeCoverPath()));
        }

        // Clean up
        dbEpisodes.clear();
        removedEpisodes.clear();
    }

    @Override
    public List<String> searchFolder() {

        Cursor cursor = com.android.leopardmediahd.leopardmediahd.MizuuApplication.getTvShowEpisodeMappingsDbAdapter().getAllFilepaths();
        com.android.leopardmediahd.functions.ColumnIndexCache cache = new com.android.leopardmediahd.functions.ColumnIndexCache();

        try {
            while (cursor.moveToNext()) // Add all show episodes in cursor to ArrayList of all existing episodes
                existingEpisodes.put(cursor.getString(cache.getColumnIndex(cursor, com.android.leopardmediahd.db.DbAdapterTvShowEpisodeMappings.KEY_FILEPATH)), "");
        } catch (Exception e) {
        } finally {
            cursor.close(); // Close cursor
            cache.clear();
        }

        TreeSet<String> results = new TreeSet<String>();

        // Do a recursive search in the file source folder
        recursiveSearch(getFolder(), results);

        List<String> list = new ArrayList<String>();

        Iterator<String> it = results.iterator();
        while (it.hasNext())
            list.add(it.next());

        return list;
    }

    @Override
    public void recursiveSearch(SmbFile folder, TreeSet<String> results) {
        try {
            if (searchSubFolders()) {
                if (folder.isDirectory()) {
                    String[] childs = folder.list();
                    for (int i = 0; i < childs.length; i++) {
                        tempSmbFile = new SmbFile(folder.getCanonicalPath() + childs[i] + "/");
                        if (tempSmbFile.isDirectory()) {
                            recursiveSearch(tempSmbFile, results);
                        } else {
                            tempSmbFile = new SmbFile(folder.getCanonicalPath() + childs[i]);
                            addToResults(tempSmbFile, results);
                        }
                    }
                } else {
                    addToResults(folder, results);
                }
            } else {
                SmbFile[] children = folder.listFiles();
                for (int i = 0; i < children.length; i++)
                    addToResults(children[i], results);
            }
        } catch (Exception e) {}
    }

    @Override
    public void addToResults(SmbFile file, TreeSet<String> results) {
        if (com.android.leopardmediahd.functions.MizLib.checkFileTypes(file.getCanonicalPath())) {
            try {
                if (file.length() < getFileSizeLimit())
                    return;
            } catch (SmbException e) {
                return;
            }

            if (!clearLibrary())
                if (existingEpisodes.get(file.getCanonicalPath()) != null) return;

            //Add the file if it reaches this point
            results.add(file.getCanonicalPath());
        }
    }

    @Override
    public SmbFile getRootFolder() {
        try {
            com.android.leopardmediahd.functions.FileSource fs = getFileSource();
            return new SmbFile(
                    com.android.leopardmediahd.functions.MizLib.createSmbLoginString(
                            fs.getDomain(),
                            fs.getUser(),
                            fs.getPassword(),
                            fs.getFilepath(),
                            true
                    ));
        } catch (Exception e) {}
        return null;
    }

    @Override
    public String toString() {
        return com.android.leopardmediahd.functions.MizLib.transformSmbPath(getRootFolder().getCanonicalPath());
    }
}