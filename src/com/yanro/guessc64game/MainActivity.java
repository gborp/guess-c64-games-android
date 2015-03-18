package com.yanro.guessc64game;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class MainActivity extends Activity {

	private static final int horizontalCells = 4;
	private static final int verticalCells = 4;
	private static final int bigPixel = 40;

	private static List<GameSlot> lstGames;
	private PuzzleManager puzzleManager;
	private static int solutionButtonIndex;
	private Puzzle puzzle;

	private boolean interactionEnabled = false;

	private int[][] avarageColorUnmaskedBitmap = new int[200 / bigPixel][320 / bigPixel];
	private boolean[][] showMask = new boolean[verticalCells][horizontalCells];
	private int[] pixels = new int[320 * 200];

	class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

		private Exception exception;

		protected Bitmap doInBackground(Void... urls) {
			try {
				puzzle = puzzleManager.getPuzzle();

				if (puzzle.unmaskedBitmap == null) {
					throw new Exception("Image cannot be decoded.");
				}

				getAvarageUnmaskedBitmapColors(puzzle.unmaskedBitmap);

				return puzzle.unmaskedBitmap;
			} catch (Exception ex) {
				this.exception = ex;

				Log.e(ex.getClass().getName(), "" + ex.getMessage(), ex);

				return null;
			}
		}

		protected void onPostExecute(Bitmap bm) {
			if (exception == null) {

				solutionButtonIndex = (int) (Math.random() * 4 + 1);
				Iterator<GameSlot> alterIter = Arrays.asList(
						puzzle.alternatives).iterator();

				switch (solutionButtonIndex) {
				case 1:
					((Button) findViewById(R.id.choice1))
							.setText(puzzle.solution.name);
					((Button) findViewById(R.id.choice2)).setText(alterIter
							.next().name);
					((Button) findViewById(R.id.choice3)).setText(alterIter
							.next().name);
					((Button) findViewById(R.id.choice4)).setText(alterIter
							.next().name);
					break;
				case 2:
					((Button) findViewById(R.id.choice2))
							.setText(puzzle.solution.name);
					((Button) findViewById(R.id.choice1)).setText(alterIter
							.next().name);
					((Button) findViewById(R.id.choice3)).setText(alterIter
							.next().name);
					((Button) findViewById(R.id.choice4)).setText(alterIter
							.next().name);
					break;
				case 3:
					((Button) findViewById(R.id.choice3))
							.setText(puzzle.solution.name);
					((Button) findViewById(R.id.choice1)).setText(alterIter
							.next().name);
					((Button) findViewById(R.id.choice2)).setText(alterIter
							.next().name);
					((Button) findViewById(R.id.choice4)).setText(alterIter
							.next().name);

					break;
				case 4:
					((Button) findViewById(R.id.choice4))
							.setText(puzzle.solution.name);
					((Button) findViewById(R.id.choice1)).setText(alterIter
							.next().name);
					((Button) findViewById(R.id.choice2)).setText(alterIter
							.next().name);
					((Button) findViewById(R.id.choice3)).setText(alterIter
							.next().name);
					break;
				}

				unmaskOneCellAndDisplay();

				enableDisableButtons(true);
			} else {
				networkError();
				// exception.printStackTrace();
			}
		}

	}

	private void getAvarageUnmaskedBitmapColors(Bitmap bm) {

		int maxCelly = 200 / bigPixel;
		int maxCellx = 320 / bigPixel;

		Bitmap bmScaled = Bitmap.createScaledBitmap(bm, maxCellx, maxCelly,
				true);
		bmScaled.getPixels(pixels, 0, maxCellx, 0, 0, maxCellx, maxCelly);

		int i = 0;

		for (int celly = 0; celly < maxCelly; celly++) {
			int[] row = avarageColorUnmaskedBitmap[celly];
			for (int cellx = 0; cellx < maxCellx; cellx++) {
				row[cellx] = pixels[i];
				i++;
			}
		}

		for (int celly = 0; celly < verticalCells; celly++) {
			for (int cellx = 0; cellx < horizontalCells; cellx++) {
				showMask[celly][cellx] = true;
			}
		}

		bm.getPixels(pixels, 0, 320, 0, 0, 320, 200);
	}

	private int dpToPx(int dp) {
		float density = getApplicationContext().getResources()
				.getDisplayMetrics().density;
		return Math.round(dp * density);
	}

	private void displayMaskedBitmap() {

		ImageView img = (ImageView) findViewById(R.id.shotimage);
		int width = 320;
		int height = 200;

		int[] maskedPixels = new int[320 * 200];

		int cellWidth = 320 / horizontalCells;
		int cellHeight = 200 / verticalCells;

		int cellOffsetY = 0;
		int px = -1;
		int ax = -1;
		int py = -1;
		int ay = -1;

		for (int celly = 0; celly < verticalCells; celly++) {

			int cellOffsetX = 0;

			for (int cellx = 0; cellx < horizontalCells; cellx++) {
				boolean mask = showMask[celly][cellx];
				py = cellOffsetY;
				int pyOffset = py * 320;
				for (int y = 0; y < cellHeight; y++) {

					ay = py / bigPixel;
					px = cellOffsetX;
					for (int x = 0; x < cellWidth; x++) {
						int pos = px + pyOffset;
						if (mask) {
							ax = px / bigPixel;
							maskedPixels[pos] = avarageColorUnmaskedBitmap[ay][ax];
						} else {
							maskedPixels[pos] = pixels[pos];
						}
						px++;
					}
					py++;
					pyOffset += 320;
				}

				cellOffsetX += cellWidth;
			}
			cellOffsetY += cellHeight;
		}

		Bitmap maskedBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		maskedBitmap.setPixels(maskedPixels, 0, 320, 0, 0, 320, 200);

		int boundingWidth = dpToPx(img.getWidth());

		float scale = ((float) boundingWidth) / width;

		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		Bitmap scaledUnmaskedBitmap = Bitmap.createBitmap(maskedBitmap, 0, 0,
				width, height, matrix, false);
		width = scaledUnmaskedBitmap.getWidth();
		height = scaledUnmaskedBitmap.getHeight();

		img.setImageBitmap(scaledUnmaskedBitmap);
	}

	public void unmaskOneCell() {
		int maskedCells = 0;
		for (int celly = 0; celly < verticalCells; celly++) {
			for (int cellx = 0; cellx < horizontalCells; cellx++) {
				if (showMask[celly][cellx]) {
					maskedCells++;
				}
			}
		}
		if (maskedCells > 0) {
			int unmaskCell = (int) (Math.random() * maskedCells);
			maskedCells = 0;
			for (int celly = 0; celly < verticalCells; celly++) {
				for (int cellx = 0; cellx < horizontalCells; cellx++) {
					if (showMask[celly][cellx]) {
						if (unmaskCell == maskedCells) {
							showMask[celly][cellx] = false;
						}
						maskedCells++;
					}
				}
			}
		}

	}

	public void unmaskAllCell() {
		for (int celly = 0; celly < verticalCells; celly++) {
			for (int cellx = 0; cellx < horizontalCells; cellx++) {
				showMask[celly][cellx] = false;
			}
		}
	}

	private void unmaskAllCellAndDisplay() {
		unmaskAllCell();
		displayMaskedBitmap();
	}

	private void unmaskOneCellAndDisplay() {
		unmaskOneCell();
		displayMaskedBitmap();
	}

	private class LoadDatabaseTask extends AsyncTask<Void, Void, Void> {

		private Exception exception;

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				lstGames = new ArrayList<GameSlot>();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(getAssets().open("games.list")));

				while (true) {
					String name = reader.readLine();
					if (name == null) {
						break;
					}
					GameSlot slot = new GameSlot();
					slot.name = name.substring(2);
					slot.url = reader.readLine().substring(2);
					ArrayList<String> lstGfx = new ArrayList<String>();
					slot.lstGfx = lstGfx;
					while (true) {
						String gfxUrl = reader.readLine();
						if (gfxUrl.charAt(0) == '-') {
							break;
						}
						lstGfx.add(gfxUrl.substring(2));
					}
					lstGfx.trimToSize();
					lstGames.add(slot);
				}
				reader.close();
				return null;
			} catch (Exception ex) {
				this.exception = ex;
				return null;
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			if (exception == null) {
				puzzleManager = new PuzzleManager();
				puzzleManager.startBackground(lstGames);

				Button startButton = ((Button) findViewById(R.id.startbutton));
				startButton.setText(R.string.start);
				startButton.setEnabled(true);
			}
		}

	}

	private void newPuzzle() {

		ImageView img = (ImageView) findViewById(R.id.shotimage);
		img.setImageResource(R.drawable.shotfiller);
		new LoadImageTask().execute();
	}

	private void start() {
		enableDisableButtons(false);

		newPuzzle();

		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		flipper.setInAnimation(this, R.anim.in_from_right);
		flipper.setOutAnimation(this, R.anim.out_to_left);
		flipper.showNext();
	}

	private void guess(int buttonIndex) {
		enableDisableButtons(false);

		unmaskAllCellAndDisplay();

		TextView gameName = (TextView) findViewById(R.id.gamename);
		TextView correctness = (TextView) findViewById(R.id.solutioncorrect);

		gameName.setText(puzzle.solution.name);

		if (buttonIndex == solutionButtonIndex) {
			correctness.setText(R.string.solution_ok);
		} else {
			correctness.setText(R.string.solution_wrong);
		}

		((Button) findViewById(R.id.next)).setEnabled(true);

		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		flipper.setInAnimation(this, R.anim.in_from_right);
		flipper.setOutAnimation(this, R.anim.out_to_left);
		flipper.showNext();
	}

	private void showNextPuzzle() {
		((Button) findViewById(R.id.next)).setEnabled(false);

		newPuzzle();
		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		flipper.setInAnimation(this, R.anim.in_from_left);
		flipper.setOutAnimation(this, R.anim.out_to_right);
		flipper.showPrevious();
	}

	private void showDetails() {
		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse(puzzle.solution.url));
		startActivity(i);
	}

	private void networkError() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle(R.string.networkerror_title);

		// set dialog message
		alertDialogBuilder
				.setMessage(R.string.networkerror_message)
				.setCancelable(false)
				.setPositiveButton(R.string.networkerror_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ViewFlipper flipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
								flipper.setInAnimation(MainActivity.this,
										R.anim.in_from_left);
								flipper.setOutAnimation(MainActivity.this,
										R.anim.out_to_right);
								flipper.showPrevious();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}

	private void enableDisableButtons(boolean enabled) {
		interactionEnabled = enabled;
		((Button) findViewById(R.id.choice1)).setEnabled(enabled);
		((Button) findViewById(R.id.choice2)).setEnabled(enabled);
		((Button) findViewById(R.id.choice3)).setEnabled(enabled);
		((Button) findViewById(R.id.choice4)).setEnabled(enabled);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		((Button) findViewById(R.id.startbutton)).setEnabled(false);

		((Button) findViewById(R.id.startbutton))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						start();
					}
				});

		((Button) findViewById(R.id.choice1))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						guess(1);
					}
				});

		((Button) findViewById(R.id.choice2))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						guess(2);
					}
				});
		((Button) findViewById(R.id.choice3))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						guess(3);
					}
				});
		((Button) findViewById(R.id.choice4))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						guess(4);
					}
				});

		((Button) findViewById(R.id.details))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						showDetails();
					}

				});
		((Button) findViewById(R.id.next))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						showNextPuzzle();
					}

				});

		((ImageView) findViewById(R.id.shotimage))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (interactionEnabled) {
							unmaskOneCellAndDisplay();
						}
					}
				});

		((ImageView) findViewById(R.id.shotimage))
				.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						if (interactionEnabled) {
							unmaskAllCellAndDisplay();
							return true;
						}
						return false;
					}

				});

		new LoadDatabaseTask().execute();
	}

	@Override
	protected void onResume() {
		if (puzzleManager != null && lstGames != null) {
			puzzleManager.startBackground(lstGames);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (puzzleManager != null) {
			puzzleManager.shutdown();
		}
		super.onPause();
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// getMenuInflater().inflate(R.menu.activity_main, menu);
	// return true;
	// }
}
