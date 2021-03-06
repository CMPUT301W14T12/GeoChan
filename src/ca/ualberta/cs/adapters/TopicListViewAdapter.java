package ca.ualberta.cs.adapters;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import ca.ualberta.cs.R;
import ca.ualberta.cs.models.PostModelList;
import ca.ualberta.cs.models.SelectedTopicModelList;
import ca.ualberta.cs.models.TopicModel;
import ca.ualberta.cs.views.TopicViewActivity;

/**
 * 
 * Maintains a list of topics
 * 
 * @author wyatt
 * 
 */
public class TopicListViewAdapter extends PostListViewAdapter<TopicModel> {

	public TopicListViewAdapter(FragmentActivity theActivity,
			PostModelList<TopicModel> arrayList) {
		super(theActivity, arrayList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.ualberta.cs.adapters.PostListViewAdapter#populateCellTitle(android
	 * .view.View, ca.ualberta.cs.models.PostModel)
	 */
	@Override
	protected void populateCellTitle(View theView, TopicModel thePost) {
		// Fill title/comment text
		TextView titleText = (TextView) theView
				.findViewById(R.id.textViewTitle);
		// List of topics, display the title
		String theTitle = thePost.getTitle();
		titleText.setText(theTitle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.ualberta.cs.adapters.PostListViewAdapter#getViewClass()
	 */
	@Override
	protected Class<?> getViewClass() {
		// TODO Auto-generated method stub
		return TopicViewActivity.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.ualberta.cs.adapters.PostListViewAdapter#setSelectedList()
	 */
	@Override
	protected void setSelectedList() {
		SelectedTopicModelList.setTopicList(theArrayList);
	}
}
