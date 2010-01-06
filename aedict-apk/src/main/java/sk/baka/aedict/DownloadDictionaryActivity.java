/**
 *     Aedict - an EDICT browser for Android
 Copyright (C) 2009 Martin Vysny
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sk.baka.aedict;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.baka.aedict.dict.DownloadDictTask;
import sk.baka.autils.DialogAsyncTask;
import sk.baka.autils.MiscUtils;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Serves for download of additional EDICT dictionaries (e.g. compdic.zip and
 * enamdict.zip).
 * 
 * @author Martin Vysny
 */
public class DownloadDictionaryActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.downloaddict);
		new DownloadDictionaryListTask().execute();
	}

	private static class DownloadableDictionaryInfo implements Comparable<DownloadableDictionaryInfo> {
		/**
		 * The dictionary printable name.
		 */
		public String name;
		/**
		 * A http URL pointing to the zipped file.
		 */
		public URL url;
		/**
		 * The size of the zip file.
		 */
		public long zippedSize;

		/**
		 * Parses a dictionary definition file line in the form of
		 * FILE_NAME,DICT_NAME,ZIPPED_LENGTH
		 * 
		 * @param line
		 *            the file line
		 * @return parsed dictionary object
		 * @throws MalformedURLException
		 *             if the URL is malformed.
		 */
		public static DownloadableDictionaryInfo parse(final String line) throws MalformedURLException {
			final String[] parsed = line.split("\\,");
			final DownloadableDictionaryInfo result = new DownloadableDictionaryInfo();
			result.name = parsed[1].trim();
			result.url = new URL(DownloadDictTask.DICT_BASE_LOCATION_URL + parsed[0].trim());
			result.zippedSize = Integer.valueOf(parsed[2].trim());
			return result;
		}

		@Override
		public String toString() {
			return name + " (" + (zippedSize / 1024) + "kB)";
		}

		public int compareTo(DownloadableDictionaryInfo another) {
			return name.compareToIgnoreCase(another.name);
		}
	}

	private static final String DICT_LIST_URL = DownloadDictTask.DICT_BASE_LOCATION_URL + "dictionaries.txt";

	/**
	 * Downloads the dictionary list and sets the correct model to the activity.
	 * 
	 * @author Martin Vysny
	 */
	private class DownloadDictionaryListTask extends DialogAsyncTask<Void, List<DownloadableDictionaryInfo>> {

		protected DownloadDictionaryListTask() {
			super(DownloadDictionaryActivity.this);
		}

		@Override
		protected void cleanupAfterError() {
			// nothing to do
		}

		@Override
		protected void onTaskSucceeded(List<DownloadableDictionaryInfo> result) {
			setListAdapter(new ArrayAdapter<DownloadableDictionaryInfo>(DownloadDictionaryActivity.this, android.R.layout.simple_list_item_1, result));
		}

		@Override
		protected List<DownloadableDictionaryInfo> protectedDoInBackground(Void... params) throws Exception {
			publishProgress(new Progress(R.string.downloadingDictionaryList, 0, 1));
			final Map<String, DownloadableDictionaryInfo> result = new HashMap<String, DownloadableDictionaryInfo>();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(DICT_LIST_URL).openStream(), "UTF-8"));
			try {
				while (true) {
					final String line = reader.readLine();
					if (line == null) {
						break;
					}
					if (MiscUtils.isBlank(line)) {
						continue;
					}
					final DownloadableDictionaryInfo info = DownloadableDictionaryInfo.parse(line);
					result.put(info.name, info);
				}
			} finally {
				MiscUtils.closeQuietly(reader);
			}
			// remove all dictionaries which are already downloaded
			result.keySet().removeAll(DownloadDictTask.listEdictDictionaries().keySet());
			final List<DownloadableDictionaryInfo> items = new ArrayList<DownloadableDictionaryInfo>(result.values());
			Collections.sort(items);
			return items;
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final DownloadableDictionaryInfo e = (DownloadableDictionaryInfo) getListAdapter().getItem(position);
		new DownloadDictTask(this, e.url, DownloadDictTask.BASE_DIR + "/index-" + e.name, e.name, e.zippedSize) {

			@Override
			protected void onTaskSucceeded(Void result) {
				new DownloadDictionaryListTask().execute();
			}

		}.execute();
	}
}
