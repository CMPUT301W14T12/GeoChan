package ca.ualberta.cs.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import ca.ualberta.cs.R;
import ca.ualberta.cs.adapters.CommentListViewAdapter;
import ca.ualberta.cs.models.ActiveUserModel;
import ca.ualberta.cs.models.CommentModelList;
import ca.ualberta.cs.models.PostModel;
import ca.ualberta.cs.models.TopicModelList;
import ca.ualberta.cs.models.UserModel;

public abstract class PostViewActivity<T extends PostModel> extends Activity {
	protected T theModel = null;
	protected CommentListViewAdapter thePostAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_view);

		// Get the selected model to display
		getSelectedModel();

		// If we have a selected model...
		if (theModel != null) {
			// Populate the view!
			populateView();
		} else {
			// finish it?
			finish();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (thePostAdapter != null) {
			TopicModelList.getInstance().unRegisterListeningAdapter(thePostAdapter);
		}
		
		Log.w("PostViewActivity", "Activity Ended.");
	}

	abstract protected void getSelectedModel();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.cellActiveArea:
			newPost();
			return true;
		case R.id.action_settings:
			startSettingsActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	protected abstract void newPost();

	/**
	 * Starts the settings activity
	 */
	protected void startSettingsActivity() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}


	protected void populateView() {
		// Add comment
		TextView commentView = (TextView) findViewById(R.id.commentTextView);
		commentView.setText(theModel.getCommentText());

		// Add score
		TextView scoreView = (TextView) findViewById(R.id.scorePostTextView);
		String scoreString = "";
		if (theModel.getScore() > 0) {
			scoreString = "+";
		}
		
		scoreString = scoreString + theModel.getScore().toString();
		scoreView.setText(scoreString);
		
		// Add Buttons
		ImageButton downVoteButton = (ImageButton) findViewById(R.id.downVoteButton);
		
		downVoteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ActiveUserModel theActiveUserModel = ActiveUserModel.getInstance();
				UserModel theLoggedInUser = theActiveUserModel.getUser();
				if (!(theLoggedInUser.getUpVoteList().contains(theModel.getId()))) {
					if (theLoggedInUser.getDownVoteList().contains(theModel.getId())) {
						theLoggedInUser.removePostIdDownVoteList(theModel.getId());
						theModel.incrementScore();
					}
					else {
						theLoggedInUser.addPostIdDownVoteList(theModel.getId());
						theModel.decrementScore();
					}
				}
				
				populateView();
				
			}
		});
		
		ImageButton upVoteButton = (ImageButton) findViewById(R.id.upVoteButton);
		
		upVoteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ActiveUserModel theActiveUserModel = ActiveUserModel.getInstance();
				UserModel theLoggedInUser = theActiveUserModel.getUser();
				if (!theLoggedInUser.getDownVoteList().contains(theModel.getId())) {
					if (theLoggedInUser.getUpVoteList().contains(theModel.getId())) {
						theLoggedInUser.removePostIdUpVoteList(theModel.getId());
						theModel.decrementScore();
					}
					else {
						theLoggedInUser.addPostIdUpVoteList(theModel.getId());
						theModel.incrementScore();
					}
				}
				
				populateView();
				
			}
		});

		// Add Author
		TextView authorView = (TextView) findViewById(R.id.authorTextView);
		authorView.setText(theModel.getPostedBy().getUserName());

		// Add or remove title text
		setTitleText();

		// Add image
		ImageView imageView = (ImageView) findViewById(R.id.imageView);
		Bitmap thePicture = theModel.getPicture();
		if (thePicture == null) {
			// No picture, hide the field
			imageView.setVisibility(View.GONE);
		} else {
			// A picture, add the image
			// TODO: Implement
			imageView.setImageBitmap(thePicture);
		}
		
		// Distance button
		Button distanceButton = (Button) findViewById(R.id.distanceButton);
		if(theModel.getLocation() != null) {
			ActiveUserModel theActiveUserModel = ActiveUserModel.getInstance();
			UserModel theLoggedInUser = theActiveUserModel.getUser();
			Location myLocation = new Location(theLoggedInUser.getLocation());
			float distanceToPost = theModel.getLocation().distanceTo(myLocation);
			String distanceButtonText = String.valueOf(distanceToPost) + " m";
			distanceButton.setText(distanceButtonText.toCharArray(), 0, distanceButtonText.length());
		}

		// Add comments
		ListView commentsListView = (ListView) findViewById(R.id.commentsListView);
		
		if (theModel.getChildrenComments() == null) {
			
		}
		else {
			// Has children!
			thePostAdapter  = new CommentListViewAdapter(this, theModel.getChildrenComments());
			CommentModelList.getInstance().registerListeningAdapter(thePostAdapter);
			commentsListView.setAdapter(thePostAdapter);
		}
		
		// Favorite Button
		ImageButton favoriteButton = (ImageButton) findViewById(R.id.favoriteButton);
		
		favoriteButton.setOnClickListener(getFavoriteOnClickListener());
		
		if (theModel.isFavorite()) {
			favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
		}
		else {
			favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
		}		
	}
	
	abstract protected OnClickListener getFavoriteOnClickListener();

	abstract void setTitleText();
	
	public void cellClicked(View theView) {
		Integer thePosition = (Integer) theView.getTag();
		
		CommentModelList commentModelList = CommentModelList.getInstance(theModel);
		commentModelList.setSelection(thePosition);
		
		Intent intent = new Intent(this, CommentViewActivity.class);
		startActivity(intent);
		
		Log.w("PostViewActivity", "the cell was clicked!");
	}

}