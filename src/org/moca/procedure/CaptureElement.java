package org.moca.procedure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.moca.ImagePreviewDialog;
import org.moca.ScalingImageAdapter;
import org.moca.activity.ProcedureRunner;
import org.moca.db.BinaryProvider;
import org.moca.db.MocaDB;
import org.moca.db.MocaDB.BinarySQLFormat;
import org.moca.media.ThumbUriAdapter;
import org.moca.util.MocaUtil;
import org.w3c.dom.Node;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class CaptureElement extends ProcedureElement implements  OnClickListener, OnItemClickListener, OnItemLongClickListener {

	 public static final String PARAMS_NAME = "keys";
	    private ScalingImageAdapter imageAdapter;
	    private static final int THUMBNAIL_SCALE_FACTOR = 5;
	    private Button captureButton;
	    private GridView imageGrid;
	    private ImagePreviewDialog imageReview;
	    private String mContentType;

	    @Override
	    public ElementType getType() {
	        return ElementType.CAPTURE;
	    }

	    /**
	     * Do not call this method! It's called /if/ CaptureElement becomes activated for display.
	     */
	    @Override
	    protected View createView(Context c) {
	        imageGrid = new GridView(c);
	        
	        String procedureId = getProcedure().getInstanceUri().getPathSegments().get(1);
	        String whereStr = BinarySQLFormat.ENCOUNTER_GUID + " = ? AND "
					+ BinarySQLFormat.ELEMENT_ID + " = ? AND "
					+ BinarySQLFormat.BINARY_VALID + " = ?";
	        
			Cursor cursor = c.getContentResolver().query(
					MocaDB.BinarySQLFormat.CONTENT_URI,
					new String[] { BinarySQLFormat._ID }, whereStr,
					new String[] { procedureId, id, "1" }, null);

			// HAXMODE -- if we don't do this we leak the Cursor 
			if (c instanceof Activity) {
				((Activity)c).startManagingCursor(cursor);
			}
			
			
	        imageAdapter = new ScalingImageAdapter(c, cursor, THUMBNAIL_SCALE_FACTOR, new ThumbUriAdapter() {
	        	public Uri getThumbnailUri(Uri inputUri) {
	        		return BinaryProvider.getThumbUri(inputUri);
	        	}
	        });
	        imageGrid.setAdapter(imageAdapter);     
	        imageGrid.setNumColumns(3);
	        imageGrid.setVerticalSpacing(5);
	        imageGrid.setPadding(5, 0, 0, 0);
	        
	        imageGrid.setOnItemClickListener(this);
	        imageGrid.setOnItemLongClickListener(this);
	        
	        //imageGrid.setTranscriptMode(imageGrid.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	        
	        captureButton = new Button(c);
	        captureButton.setText("Enter");
	        captureButton.setOnClickListener(this);
	        
	        imageReview = new ImagePreviewDialog(c);
	        LinearLayout picContainer = new LinearLayout(c);
	        picContainer.setOrientation(LinearLayout.VERTICAL);

	        //Set question
	        TextView tv = new TextView(c);
	        tv.setText(question);
	        tv.setGravity(Gravity.CENTER);
	        tv.setTextAppearance(c, android.R.style.TextAppearance_Medium);
	        
	        //Add to layout
	        picContainer.addView(tv, new LinearLayout.LayoutParams(-1,-1,0.1f));
	        //picContainer.addView(imageView, new LinearLayout.LayoutParams(-1,-1,0.1f));
	        
	        //Add button
	        picContainer.addView(captureButton, new LinearLayout.LayoutParams(-1,-1,0.1f));
	        picContainer.addView(imageGrid, new LinearLayout.LayoutParams(-1, 210)); //LayoutParams(-1,-1,0.8f));
	        picContainer.setWeightSum(1.0f);
	        
	        return picContainer;
	    }
	    
	    // Sends an intent to ProcedureRunner with the image 
	    // parameters. The native camera app is then called from 
	    // ProcedureRunner.
		 public void onClick(View v) {
			 if (v == captureButton) {
				 String procedureId = getProcedure().getInstanceUri().getPathSegments().get(1); //which procedure its part of
				 String[] params = {procedureId, id};
			
				 Intent captureIntent = new Intent(getContext(), ProcedureRunner.class);
				 captureIntent.putExtra(PARAMS_NAME, params)
				 			  .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
				              .putExtra("intentKey", ProcedureRunner.CAPTURE_INTENT_REQUEST_CODE);
				 captureIntent.setType(mContentType);
				 ((Activity) getContext()).startActivity(captureIntent);
			 }
		 }
		 
		 
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Log.i(TAG, "onItemClick pos:" + position + " id:" + id);
			imageAdapter.toggleSelection(id);
			//v.postInvalidate();
			v.invalidate();
			//SelectableImageView view = (SelectableImageView) v;
			//view.toggleMultiSelected();
			//view.invalidate();
			//iadapter.notifyDataSetChanged();
			//parent.invalidate();
			
		}

		/* (non-Javadoc)
		 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
		 */
		/**
		 * If a thumbnail is clicked, open up an image preview dialog.
		 */
		public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
				long id) {
			Log.i(TAG, "" + position);
			
//			long imageId = imageAdapter.getItemId(position);
//			Uri imageUri = ContentUris.withAppendedId(BinarySQLFormat.CONTENT_URI,
//					imageId);
//			try {
//				// hack for in-emulator demo, commented out here, but use to load a
//				// static image
//				// imageReview.showImage(getContext().getContentResolver().openInputStream(Uri.parse("android.resource://org.moca/"
//				// + R.drawable.incision2)));
//				imageReview.showImage(imageUri);
//				imageReview.show();	
//			} catch (IOException e) {
//				Log.e(TAG, "Can't open the image file for uri " + imageUri);
//			}
			return false;
		}
	    
		public void setAnswer(String answer) {
			if(!isViewActive()) {
				this.answer = answer;
			} else {
				// TODO : Fix this so that it works! We have the id # of the picture, and we need to reset 'selected' to match this such that
				// iadapter.getItemId(selected) == the answer we have here.
				String[] ids = answer.split(",");
				for(String id : ids) {
					imageAdapter.setSelected(Long.parseLong(id), true);
				}
			}
		}
	    
	    public String getAnswer() {
//	    	old stuff from single selection
	        if(!isViewActive())
	            return answer;
//	        Log.i(TAG, "Putting answer " + selected);
//	        return iadapter.getItemId(selected) + "";
	    	

	        ArrayList<String> answerz = new ArrayList<String>();
	    	/*for(Entry<Long,SelectableImageView> e : iadapter.getViewMap().entrySet()) {
	    		Log.i(TAG, "getAnswer -- item " + e.getKey());
	    		if(e.getValue().isMultiSelected()) {
	    			answerz.add(Long.toString(e.getKey()));
	    			Log.i(TAG, "getAnswer -- item selected" + e.getKey());
	    		}
	    	}*/
	    	
	    	
			for (int i = 0; i < imageAdapter.getCount(); i++) {
				Long id = imageAdapter.getItemId(i);
				Log.i(TAG, "Considering element " + id + " for selection.");
				if (imageAdapter.isSelected(id)) {
					Log.i(TAG, "Element " + id + " is selected.");
					answerz.add(Long.toString(id));
				}
			}
			
	    	if (answerz.size() > 0) {
	    		StringBuilder csv = new StringBuilder(answerz.get(0));
	    		for (int i=1; i<answerz.size(); i++) {
	    			csv.append(",");
	    			csv.append(answerz.get(i));
	    		}
	    		Log.i(TAG, "getAnswers is returning " + csv.toString());
	    		return csv.toString();
	    	} else {
	    		Log.i(TAG, "getAnswers is returning blank");
	    		return "";
	    	}
	    }
	    
	    @Override
	    protected void populateElementAttributeMap(Map<String, String> elementAttributeMap) {
	    	super.populateElementAttributeMap(elementAttributeMap);
	    	elementAttributeMap.put("content_type", mContentType);
	    }
	    
	    /**
	     * Make question and response into an XML string for storing or transmission.
	     */
	    public void buildXML(StringBuilder sb) {
	        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
	        sb.append("\" question=\"" + question);
	        sb.append("\" answer=\"" + getAnswer());
	        sb.append("\" concept=\"" + getConcept());
	        sb.append("\"/>\n");
	    }
	    
	    private CaptureElement(String id, String question, String answer, String concept, String figure, String audio, String contentType) {
	        super(id, question, answer, concept, figure, audio);
	        mContentType = contentType;
	    }
	    
	    /**
	     * Create a CaptureElement from an XML procedure definition.
	     */
	    public static CaptureElement fromXML(String id, String question, String answer, String concept, String figure, String audio, Node node) throws ProcedureParseException {
	    	String contentType = MocaUtil.getNodeAttributeOrFail(node, "content-type", new ProcedureParseException("Required attribute 'content-type' not provided for CAPTURE element."));
	        return new CaptureElement(id, question, answer, concept, figure, audio, contentType);
	    }
	    
}
