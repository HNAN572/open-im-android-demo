package io.openim.android.ouimoments.widgets.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.bean.CommentItem;
import io.openim.android.ouimoments.mvp.presenter.CirclePresenter;
import io.openim.android.ouimoments.utils.DatasUtil;

/**
 * 
* @ClassName: CommentDialog 
* @Description: 评论长按对话框，保护复制和删除 
* @author yiw
* @date 2015-12-28 下午3:36:39 
*
 */
public class CommentDialog extends Dialog implements
		View.OnClickListener {

	private Context mContext;
	private CirclePresenter mPresenter;
	private CommentItem mCommentItem;
	private String momentID;
	private int mCirclePosition;

	public CommentDialog(Context context, CirclePresenter presenter,
			CommentItem commentItem, String momentID,int circlePosition) {
		super(context, R.style.comment_dialog);
		mContext = context;
        this.momentID=momentID;
		this.mPresenter = presenter;
		this.mCommentItem = commentItem;
		this.mCirclePosition = circlePosition;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_comment);
		initWindowParams();
		initView();
	}

	private void initWindowParams() {
		Window dialogWindow = getWindow();
		// 获取屏幕宽、高用
		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = (int) (display.getWidth() * 0.65); // 宽度设置为屏幕的0.65

		dialogWindow.setGravity(Gravity.CENTER);
		dialogWindow.setAttributes(lp);
	}

	private void initView() {
		TextView copyTv = (TextView) findViewById(R.id.copyTv);
		copyTv.setOnClickListener(this);
		TextView deleteTv = (TextView) findViewById(R.id.deleteTv);
		if (mCommentItem != null
				&& DatasUtil.curUser.getId().equals(
						mCommentItem.getUser().getId())) {
			deleteTv.setVisibility(View.VISIBLE);
		} else {
			deleteTv.setVisibility(View.GONE);
		}
		deleteTv.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.copyTv) {
            if (mCommentItem != null) {
                ClipboardManager clipboard =
                    (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(mCommentItem.getContent());
            }
            dismiss();
        } else if (id == R.id.deleteTv) {
            if (mPresenter != null && mCommentItem != null) {
                mPresenter.deleteComment(momentID, mCirclePosition, mCommentItem.getId());
            }
            dismiss();
        }
	}

}
