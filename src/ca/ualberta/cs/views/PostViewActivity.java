package ca.ualberta.cs.views;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import ca.ualberta.cs.R;
import ca.ualberta.cs.adapters.CommentListViewAdapter;
import ca.ualberta.cs.controllers.PostViewController;
import ca.ualberta.cs.models.ActiveUserModel;
import ca.ualberta.cs.models.CommentModelList;
import ca.ualberta.cs.models.EditPostModel;
import ca.ualberta.cs.models.PostModel;
import ca.ualberta.cs.models.TopicModelList;
import ca.ualberta.cs.providers.LocationProvider;

/**
 * An abstract activity that displays a post
 * 
 * @author wyatt
 * 
 * @param <T>
 */
public abstract class PostViewActivity<T extends PostModel> extends Activity
		implements LocationUpdatedInterface {
	protected static Bitmap currentBitmap = null;

	/**
	 * @return the currentBitmap
	 */
	public static Bitmap getCurrentBitmap() {
		return currentBitmap;
	}

	protected ListView commentsListView;

	protected OnClickListener favoriteOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ImageButton favoritesButton = (ImageButton) v;
			theController.toggleFavorite(theModel);
			populateFavoritesButton(favoritesButton);
		}
	};

	protected LinearLayout headerView = null;

	private Menu menu;

	protected PostViewController<T> theController;

	protected T theModel = null;

	protected CommentListViewAdapter thePostAdapter;

	abstract protected void editPost();

	/**
	 * Populates theModel with the proper selected model
	 * 
	 * @return TODO
	 */
	abstract protected T getSelectedModel();

	/**
	 * Gets the title string associated with the currently displayed post.
	 * 
	 * @return
	 */
	abstract protected String getTitleString();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.ualberta.cs.views.LocationUpdatedInterface#locationWasUpdated(android
	 * .location.Location)
	 */
	@Override
	public void locationWasUpdated(Location theNewLocation) {
		// Redraws the boxes that contain distance information
		populateDistanceButton();
		populateCommentsView();
	}

	/**
	 * Opens a map, triggered via XML
	 * @param theView
	 */
	public abstract void onClick_OpenMap(View theView);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_post_view);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Populate the model
		this.theModel = getSelectedModel();

		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.headerView = (LinearLayout) inflator.inflate(R.layout.post_header,
				null);

		commentsListView = (ListView) findViewById(R.id.commentsListView);
		commentsListView.addHeaderView(headerView);

		// Populate the view
		if (this.theModel == null) {
			throw new RuntimeException(
					"Tried to execute the view without selecting anything? (No idea how you got here...)");
		}

		// Register for location updates
		LocationProvider.getInstance(null).registerForLocationUpdates(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// Grab the menu for later...
		this.menu = menu;

		// Read later text
		updateReadLaterText();

		// Setup the menu
		setupTheMenu();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (thePostAdapter != null) {
			TopicModelList.getInstance().unRegisterListeningAdapter(
					thePostAdapter);
		}

		LocationProvider.getInstance(null).unregisterForLocationUpdates(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.cellActiveArea:
			replyToPost();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.readLaterButton:
			onReadLaterPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem refreshIcon = menu.findItem(R.id.refreshButton);
		refreshIcon.setVisible(false);

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Is fired when the read later button is clicked in the model
	 */
	private void onReadLaterPressed() {
		theController.toggleReadLater(getSelectedModel());

		updateReadLaterText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// Populate the view!
		populateView();
	}

	/**
	 * Populates the comments view
	 */
	private void populateCommentsView() {
		CommentModelList theCommentModelList = CommentModelList
				.getInstanceFromParent(theModel);

		// Has children!
		thePostAdapter = new CommentListViewAdapter(this, theCommentModelList);
		commentsListView.setAdapter(thePostAdapter);
	}

	/**
	 * Populates the distance button
	 */
	private void populateDistanceButton() {
		Button distanceButton = (Button) this.headerView
				.findViewById(R.id.distanceButton);

		if (theModel.getLocation() != null) {
			if (ActiveUserModel.getInstance().getUser().getLocation() != null) {
				Location userLocation = new Location(ActiveUserModel
						.getInstance().getUser().getLocation());
				float distanceToPost = userLocation.distanceTo(theModel
						.getLocation()) / 1000;
				String distanceButtonText = String.format("%.2f",
						distanceToPost) + " km";

				distanceButton.setText(distanceButtonText.toCharArray(), 0,
						distanceButtonText.length());
			} else {
				distanceButton.setText(theModel.getLocationAsString());
			}
		} else {
			distanceButton.setText("Location");
		}

	}

	/**
	 * Populates the edit button
	 */
	private void populateEditButton() {
		// get the edit button if required
		if (theModel.getPostedBy().getUserHash()
				.equals(ActiveUserModel.getInstance().getUser().getUserHash())) {
			// set the visibility to visible
			Button editButton = (Button) this.headerView
					.findViewById(R.id.editButton);
			editButton.setVisibility(View.VISIBLE);

			// add onclick listener
			editButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					editPost();
				}
			});
		}
	}

	/**
	 * Populates the favorites button
	 * @param favoriteButton
	 */
	private void populateFavoritesButton(ImageButton favoriteButton) {
		favoriteButton.setOnClickListener(favoriteOnClickListener);

		if (theModel.isFavorite()) {
			favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
		} else {
			favoriteButton
					.setImageResource(android.R.drawable.btn_star_big_off);
		}
	}

	/** 
	 * Populates the image view
	 * @param imageView
	 */
	private void populateImageView(Button imageView) {
		final Bitmap thePicture = theModel.getPicture();
		if (thePicture == null) {
			// No picture, hide the field
			imageView.setVisibility(View.GONE);
		} else {
			// A picture, add the image
			currentBitmap = thePicture;

			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(PostViewActivity.this,
							PictureViewActivity.class);
					intent.putExtra(PictureViewActivity.TITLE_KEY,
							PostViewActivity.this.getTitleString());
					startActivity(intent);
				}
			});
		}
	}

	/**
	 * Sets up the score controllers and views
	 * @param scoreView
	 * @param downVoteButton
	 * @param upVoteButton
	 */
	private void populateScoreControlsAndView(final TextView scoreView,
			final ImageButton downVoteButton, final ImageButton upVoteButton) {

		populateScoreField(scoreView);

		if (TopicModelList.getInstance().getLastSelection() != null) {

			downVoteButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (theController.decreaseScore(theModel)) {
						populateScoreField(scoreView);
					}
				}
			});

			upVoteButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (theController.increaseScore(theModel)) {
						populateScoreField(scoreView);
					}
				}

			});

		} else {
			downVoteButton.setVisibility(View.GONE);
			upVoteButton.setVisibility(View.GONE);
		}
	}

	/**
	 * Populates the score view
	 * @param scoreView
	 */
	protected void populateScoreField(final TextView scoreView) {
		Integer theScore = theModel.getScore();
		scoreView.setText(theScore.toString());
	}

	/**
	 * Populates the view for the post model
	 */
	protected void populateView() {

		// Add comment
		TextView commentView = (TextView) this.headerView
				.findViewById(R.id.commentTextView);
		commentView.setText(theModel.getCommentText());

		// Add score
		final TextView scoreView = (TextView) this.headerView
				.findViewById(R.id.scorePostTextView);

		// Add Buttons
		final ImageButton downVoteButton = (ImageButton) this.headerView
				.findViewById(R.id.downVoteButton);
		final ImageButton upVoteButton = (ImageButton) this.headerView
				.findViewById(R.id.upVoteButton);
		populateScoreControlsAndView(scoreView, downVoteButton, upVoteButton);

		// Add Date
		TextView dateView = (TextView) this.headerView
				.findViewById(R.id.ageTextView);
		String date = (String) DateFormat.format("yyyy/MM/dd",
				theModel.getDatePosted());
		dateView.setText(date);

		// Add Author
		TextView authorView = (TextView) this.headerView
				.findViewById(R.id.authorTextView);
		authorView.setText(theModel.getPostedBy().getUserName());

		// Add or remove title text
		setTitleText();

		// Add image
		Button imageViewButton = (Button) this.headerView
				.findViewById(R.id.pictureButton);
		populateImageView(imageViewButton);

		// add edit if required
		populateEditButton();

		// Distance button
		populateDistanceButton();

		// Add comments
		populateCommentsView();

		// Favorite Button
		ImageButton favoriteButton = (ImageButton) this.headerView
				.findViewById(R.id.favoriteButton);
		populateFavoritesButton(favoriteButton);
	}

	/**
	 * Starts an activity to reply to the currently visible post
	 */
	protected void replyToPost() {
		EditPostModel.getInstance().setTheParent(theModel);

		Intent intent = new Intent(this, EditCommentActivity.class);
		startActivity(intent);
	}

	abstract void setTitleText();

	/**
	 * Sets up the menu
	 * @param theMenu
	 */
	void setupTheMenu() {
		if (TopicModelList.getInstance().getLastSelection() == null) {
			// Find the edit button
			MenuItem editButton = this.menu.findItem(R.id.cellActiveArea);
			editButton.setVisible(false);
		}
	}

	/**
	 * Updates the read later text from the menu
	 */
	private void updateReadLaterText() {
		MenuItem theReadLaterButton = this.menu.findItem(R.id.readLaterButton);

		if (this.theModel.isReadLater()) {
			theReadLaterButton.setTitle("Mark as read");
		} else {
			theReadLaterButton.setTitle("Read Later");
		}
	}
}