package com.yanro.guessc64game;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.graphics.BitmapFactory;

public class PuzzleManager {

	private static final int NO_PRECACHED_PUZZLES = 4;

	private ArrayBlockingQueue<Puzzle> quPuzzle = new ArrayBlockingQueue<Puzzle>(
			NO_PRECACHED_PUZZLES, true);
	private Thread backgroundThread;
	private volatile boolean shutdown;
	private List<GameSlot> lstGames;

	public PuzzleManager() {

	}

	public void shutdown() {
		shutdown = true;
	}

	public void startBackground(List<GameSlot> lstGames) {
		this.lstGames = lstGames;
		shutdown = false;
		backgroundThread = new Thread(new BackgroundThread());
		backgroundThread.start();
	}

	private class BackgroundThread implements Runnable {

		@Override
		public void run() {
			while (!shutdown) {
				try {
					Puzzle puzzle = newPuzzle();
					if (puzzle != null) {
						quPuzzle.offer(puzzle, 1, TimeUnit.MINUTES);
					} else {
						Thread.sleep(5000);
					}
				} catch (InterruptedException e) {
					// it's okay
				}
			}

		}

	}

	private Puzzle newPuzzle() {
		try {
			Puzzle result = new Puzzle();

			int solutionIndex = (int) (Math.random() * lstGames.size());

			int alternative1 = solutionIndex;
			while (alternative1 == solutionIndex) {
				alternative1 = (int) (Math.random() * lstGames.size());
			}
			int alternative2 = solutionIndex;
			while (alternative2 == solutionIndex
					|| alternative2 == alternative1) {
				alternative2 = (int) (Math.random() * lstGames.size());
			}

			int alternative3 = solutionIndex;
			while (alternative3 == solutionIndex
					|| alternative3 == alternative1
					|| alternative3 == alternative2) {
				alternative3 = (int) (Math.random() * lstGames.size());
			}

			result.alternatives = new GameSlot[] { lstGames.get(alternative1),
					lstGames.get(alternative2), lstGames.get(alternative3) };

			GameSlot solution = lstGames.get(solutionIndex);
			result.solution = solution;

			String gfxUrl = solution.lstGfx
					.get((int) (Math.random() * solution.lstGfx.size()));

			URL url = new URL("http://www.lemon64.com/games/screenshots/full"
					+ gfxUrl);
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			result.unmaskedBitmap = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();

			return result;
		} catch (Exception ex) {
			return null;
		}
	}

	public Puzzle getPuzzle() {
		try {
			return quPuzzle.poll(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
