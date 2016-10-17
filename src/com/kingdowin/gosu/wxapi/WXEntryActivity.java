package com.kingdowin.gosu.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.kingdowin.gosu.R;
import com.kingdowin.gosu.third.Constant;
import com.kingdowin.gosu.third.QQLogin;
import com.kingdowin.gosu.third.WeiBoLogin;
import com.kingdowin.gosu.third.WeiXinLogin;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements OnClickListener,
		IWXAPIEventHandler {
	private Button qq_login, qq_logout, weibo_login, weixin_login;
	private IWXAPI api;
	private QQLogin qqLogin;
	private WeiBoLogin weiboLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		qq_login = (Button) findViewById(R.id.qq_login);
		qq_logout = (Button) findViewById(R.id.qq_logout);
		weibo_login = (Button) findViewById(R.id.weibo_login);
		weixin_login = (Button) findViewById(R.id.weixin_login);
		qq_login.setOnClickListener(this);
		qq_logout.setOnClickListener(this);
		weibo_login.setOnClickListener(this);
		weixin_login.setOnClickListener(this);

		qqLogin = new QQLogin(this);
		weiboLogin = new WeiBoLogin(this);
		api = WXAPIFactory.createWXAPI(this, Constant.WEIXIN_APP_ID, false);
		api.handleIntent(getIntent(), this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.qq_login:
			if (!QQLogin.mTencent.isSessionValid()) {
				QQLogin.mTencent.login(this, "all", qqLogin);
			}
			break;
		case R.id.qq_logout:
			if (QQLogin.mTencent.isSessionValid()) {
				QQLogin.mTencent.logout(this);
			}
			break;
		case R.id.weixin_login:
			api.registerApp(Constant.WEIXIN_APP_ID);
			SendAuth.Req req = new SendAuth.Req();
			req.scope = "snsapi_userinfo";
			req.state = "wechat_sdk_demo";
			api.sendReq(req);
			break;
		case R.id.weibo_login:
			WeiBoLogin.mSsoHandler.authorizeWeb(weiboLogin);
			break;
		}
	}

	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {

		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			break;
		default:
			break;
		}
	}

	@Override
	public void onResp(BaseResp resp) {
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			String code = ((SendAuth.Resp) resp).code;
			new WeiXinLogin(this).getWeiXinInfo(code);
			Toast.makeText(this, R.string.auth_success, Toast.LENGTH_SHORT)
					.show();
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			Toast.makeText(this, R.string.auth_cancel, Toast.LENGTH_SHORT)
					.show();
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			Toast.makeText(this, R.string.auth_failure, Toast.LENGTH_SHORT)
					.show();
			break;
		default:
			break;
		}

	}
}
