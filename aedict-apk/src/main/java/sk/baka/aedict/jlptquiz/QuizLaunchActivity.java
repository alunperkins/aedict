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

package sk.baka.aedict.jlptquiz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.R;
import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.KanjidicEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

/**
 * Allows user to select JLPT levels to test and start the quiz.
 * 
 * @author Martin Vysny
 */
public class QuizLaunchActivity extends Activity {
	/**
	 * Maps checkbox component ID to JLPT level.
	 */
	private final Map<Integer, Integer> jlptMap = new HashMap<Integer, Integer>();
	{
		jlptMap.put(R.id.jlptLevel1, 1);
		jlptMap.put(R.id.jlptLevel2, 2);
		jlptMap.put(R.id.jlptLevel3, 3);
		jlptMap.put(R.id.jlptLevel4, 4);
		jlptMap.put(R.id.jlptLevel5, 5);
		jlptMap.put(R.id.jlptLevel6, 6);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!AedictApp.getDownloader().checkDic(this, DictTypeEnum.Kanjidic)) {
			finish();
			return;
		}
		setContentView(R.layout.jlpt_quiz_launch);
		for (final Map.Entry<Integer, Integer> e : jlptMap.entrySet()) {
			final CheckBox cb = (CheckBox) findViewById(e.getKey());
			cb.setText(AedictApp.format(R.string.jlptLevelX, e.getValue()));
		}
		findViewById(R.id.launch).setOnClickListener(AndroidUtils.safe(this, new View.OnClickListener() {

			public void onClick(View v) {
				final Set<Integer> jlpt = new HashSet<Integer>();
				for (final Map.Entry<Integer, Integer> e : jlptMap.entrySet()) {
					final CheckBox cb = (CheckBox) findViewById(e.getKey());
					if (cb.isChecked()) {
						jlpt.add(e.getValue());
					}
				}
				if (jlpt.isEmpty()) {
					return;
				}
				try {
					final List<KanjidicEntry> questions = generateQuestions(jlpt);
					QuizActivity.launch(QuizLaunchActivity.this, questions);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}));
	}

	private List<KanjidicEntry> generateQuestions(final Set<Integer> jlpts) throws IOException {
		final StringBuilder kanjiPool = new StringBuilder();
		for (final Integer level : jlpts) {
			kanjiPool.append(KanjiUtils.getJlptKanjis(level));
		}
		final List<KanjidicEntry> questions = new ArrayList<KanjidicEntry>();
		final Random r = new Random();
		final LuceneSearch search = new LuceneSearch(DictTypeEnum.Kanjidic, null, true);
		try {
			for (int i = 0; i < QUIZ_QUESTION_COUNT; i++) {
				if (kanjiPool.length() == 0) {
					break;
				}
				final int index = r.nextInt(kanjiPool.length());
				final char kanji = kanjiPool.charAt(index);
				kanjiPool.deleteCharAt(index);
				final List<DictEntry> result = search.search(SearchQuery.kanjiSearch(kanji, null, null));
				for (final Iterator<? extends DictEntry> it = result.iterator(); it.hasNext();) {
					final DictEntry e = it.next();
					if (!e.isValid()) {
						throw new RuntimeException(e.english);
					}
				}
				questions.add((KanjidicEntry) result.get(0));
			}
		} finally {
			MiscUtils.closeQuietly(search);
		}
		return questions;
	}

	private static final int QUIZ_QUESTION_COUNT = 20;
}
