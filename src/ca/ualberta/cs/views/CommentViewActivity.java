/**
 * 
 */
package ca.ualberta.cs.views;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import ca.ualberta.cs.R;
import ca.ualberta.cs.models.CommentModel;
import ca.ualberta.cs.models.CommentModelList;

/**
 * @author wyatt
 *
 */
public class CommentViewActivity extends PostViewActivity<CommentModel> {

	@Override
	protected void getSelectedModel() {
		// TODO Auto-generated method stub
		this.theModel = CommentModelList.getInstance().getSelection();
	}

	@Override
	void setTitleText() {
		// Hide the titleView
		TextView titleView = (TextView) findViewById(R.id.titleTextView);
		titleView.setVisibility(View.GONE);
		
		// Fix action bar
		getActionBar().setTitle(theModel.getCommentText()); 
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		CommentModelList.getInstance().setSelection(theModel);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		CommentModelList.getInstance().resetSelection();
	}

	@Override
	protected OnClickListener getFavoriteOnClickListener() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void newPost() {
		Intent intent = new Intent(this, EditCommentActivity.class);
		intent.putExtra(EditCommentActivity.IS_NEW, true);
		startActivity(intent);
	}
}
