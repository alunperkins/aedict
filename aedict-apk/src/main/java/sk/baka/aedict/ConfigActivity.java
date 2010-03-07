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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.DownloadDictTask;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.MiscUtils;
import sk.baka.autils.bind.AndroidViewMapper;
import sk.baka.autils.bind.Binder;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

/**
 * Configures AEdict.
 * 
 * @author Martin Vysny
 */
public class ConfigActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// components are now initialized in onResume phase, to refresh
		// dictionary list when a new dictionary is downloaded
		// fill in the components
		final Config cfg = AedictApp.loadConfig();
		final Spinner dictPicker = (Spinner) findViewById(R.id.spinDictionaryPicker);
		final List<String> dictionaries = new ArrayList<String>(DownloadDictTask.listEdictDictionaries().keySet());
		Collections.sort(dictionaries);
		dictPicker.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dictionaries));
		dictPicker.setOnItemSelectedListener(new ModificationHandler());
		new Binder().bindFromBean(cfg, new AndroidViewMapper(true), this, false);
		// add modification handlers
		final CheckBox cfgNotifBar = (CheckBox) findViewById(R.id.cfgNotifBar);
		cfgNotifBar.setOnCheckedChangeListener(new ModificationHandler());
		final CheckBox cfgUseRomaji = (CheckBox) findViewById(R.id.cfgUseRomaji);
		cfgUseRomaji.setOnCheckedChangeListener(new ModificationHandler());
		final Button cleanup = (Button) findViewById(R.id.cleanupEdictFilesButton);
		cleanup.setOnClickListener(AndroidUtils.safe(this, new View.OnClickListener() {

			public void onClick(View v) {
				cleanup();
			}

		}));
		final Button showInfoDialogs = (Button) findViewById(R.id.showInfoDialogsButton);
		showInfoDialogs.setOnClickListener(AndroidUtils.safe(this, new View.OnClickListener() {

			public void onClick(View v) {
				final DialogUtils utils = new DialogUtils(ConfigActivity.this);
				utils.clearInfoOccurency();
				utils.showToast(R.string.showInfoDialogsEnabled);
			}

		}));
		final Spinner s = (Spinner) findViewById(R.id.romanizationSystem);
		s.setOnItemSelectedListener(new ModificationHandler());
		AbstractActivity.setButtonActivityLauncher(this, R.id.btnDownloadDictionary, DownloadDictionaryActivity.class);
	}

	private class ModificationHandler implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			saveConfig();
		}

		private void saveConfig() {
			try {
				final Config cfg = new Config();
				new Binder().bindToBean(cfg, new AndroidViewMapper(true), ConfigActivity.this, false);
				AedictApp.saveConfig(cfg);
			} catch (Exception ex) {
				Log.e("ConfigActivity", "Exception thrown", ex);
				new DialogUtils(ConfigActivity.this).showErrorDialog("An application problem occured: " + ex.toString());
			}
		}

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			saveConfig();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	/**
	 * Deletes all dictionary files.
	 */
	private void cleanup() {
		final DialogUtils utils = new DialogUtils(this);
		utils.showYesNoDialog(AedictApp.format(R.string.deleteDictionaryFiles, MiscUtils.getLength(new File(DictTypeEnum.BASE_DIR)) / 1024), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					MiscUtils.deleteDir(new File(DictTypeEnum.BASE_DIR));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				utils.showToast(R.string.data_files_removed);
			}

		});
	}
}
