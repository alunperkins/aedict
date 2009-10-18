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

import sk.baka.aedict.AedictApp.Config;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

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
		final CheckBox cfgNotifBar = (CheckBox) findViewById(R.id.cfgNotifBar);
		cfgNotifBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				final Config cfg = new Config(false);
				cfg.isAlwaysAvailable = isChecked;
				AedictApp.saveConfig(cfg);
			}
		});
		final Button cleanup = (Button) findViewById(R.id.cleanupEdictFilesButton);
		cleanup.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				cleanup();
			}

		});
	}

	private void cleanup() {
		final SearchUtils utils = new SearchUtils(this);
		utils.showYesNoDialog(AedictApp.format(R.string.deleteEdictFiles, MiscUtils.getLength(new File(DownloadEdictTask.BASE_DIR)) / 1024), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					MiscUtils.deleteDir(new File(DownloadEdictTask.BASE_DIR));
					utils.showInfoDialog(R.string.data_files_removed);
				} catch (Exception ex) {
					Log.e(MainActivity.class.getSimpleName(), ex.toString(), ex);
					utils.showErrorDialog(getString(R.string.failed_to_clean_files) + ex);
				}
			}

		});
	}
}