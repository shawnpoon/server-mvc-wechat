package com.joosure.server.mvc.wechat.controller;

import java.net.UnknownHostException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.sword.wechat4j.oauth.OAuthException;

import com.joosure.server.mvc.wechat.constant.WechatConstant;
import com.joosure.server.mvc.wechat.entity.domain.AjaxResult;
import com.joosure.server.mvc.wechat.entity.domain.BaseResult;
import com.joosure.server.mvc.wechat.entity.domain.Redirecter;
import com.joosure.server.mvc.wechat.entity.domain.UserInfo;
import com.joosure.server.mvc.wechat.entity.domain.page.AddItemPageInfo;
import com.joosure.server.mvc.wechat.entity.domain.page.BasePageInfo;
import com.joosure.server.mvc.wechat.entity.domain.page.HomePageInfo;
import com.joosure.server.mvc.wechat.entity.domain.page.MePageInfo;
import com.joosure.server.mvc.wechat.service.ItemService;
import com.joosure.server.mvc.wechat.service.SystemFunctionService;
import com.joosure.server.mvc.wechat.service.SystemLogStorageService;
import com.joosure.server.mvc.wechat.service.UserService;
import com.joosure.server.mvc.wechat.service.WechatNativeService;
import com.joosure.server.mvc.wechat.service.WechatWebService;
import com.shawn.server.core.http.RequestHandler;
import com.shawn.server.core.http.ResponseHandler;
import com.shawn.server.core.util.JsonUtil;
import com.shawn.server.core.util.StringUtil;

/**
 * 处理Wechat端来自页面的请求<br>
 * 第一期全部链接使用手动授权，第二期更新为先静默授权再手动授权 <br>
 * <br>
 * bugs：<br>
 * 1.Ajax接口目前没有接入日志
 * 
 * @author Shawnpoon
 */
@RequestMapping("/wechat")
@Controller
public class WechatWebController {

	@Autowired
	private WechatWebService wechatWebService;
	@Autowired
	private WechatNativeService wechatNativeService;
	@Autowired
	private SystemLogStorageService logService;
	@Autowired
	private ItemService itemService;
	@Autowired
	private SystemFunctionService systemFunctionService;
	@Autowired
	private UserService userService;

	/**
	 * 多重跳转路由
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/redirecter")
	public String redirecter(HttpServletRequest request, HttpServletResponse response, Model model) {
		String redirectURL = "error/404";
		try {
			Redirecter redirecter = wechatWebService.redirecter(request);
			if (redirecter.isValid()) {
				redirectURL = "redirect:" + redirecter.getRedirectURL();
			}
		} catch (Exception e) {
			return errorPageRouter(e, "WechatWebController.redirecter");
		}
		return redirectURL;
	}

	/**
	 * 发送checkcode - ajax
	 * 
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping("/sendCheckCode")
	public void sendCheckCode(HttpServletRequest request, HttpServletResponse response, Model model) {
		AjaxResult ar = new AjaxResult();
		try {
			String eo = request.getParameter("eo");
			String mobile = request.getParameter("mobile");
			if (StringUtil.isBlank(mobile) || StringUtil.isBlank(eo)) {
				ar.setErrCode("9002");
			} else {
				if (userService.getUserByMobile(mobile) != null) {
					ar.setErrCode("9003");
					ar.setErrMsg("该号码已经被注册");
				} else {
					BaseResult baseResult = systemFunctionService.sendCheckCode(mobile, eo);
					ar.setErrCode(baseResult.getErrCode() + "");
					ar.setErrMsg(baseResult.getErrMsg());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			ar.setErrCode("9001");
		}

		String json = JsonUtil.Object2JsonStr(ar);
		ResponseHandler.output(response, json);
	}

	/**
	 * 首页
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/home")
	public String home(HttpServletRequest request, HttpServletResponse response, Model model) {
		try {
			HomePageInfo homePageInfo = wechatWebService.homePage(request);

			pageLogger(request, "/wechat/home", homePageInfo);
		} catch (Exception e) {
			return errorPageRouter(e, "WechatWebController.home");
		}
		return "home";
	}

	/**
	 * 个人页
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/me")
	public String me(HttpServletRequest request, HttpServletResponse response, Model model) {
		try {
			MePageInfo pageInfo = wechatWebService.mePage(request);
			model.addAttribute("tableUrls", pageInfo.getTableURLs());
			model.addAttribute("eo", pageInfo.getUserInfo().getEncodeOpenid());
			model.addAttribute("user", pageInfo.getUserInfo().getUser());
			model.addAttribute("jsapi", pageInfo.getJsApiParam());

			String redirectUrl = registeredValidAndRedirect(pageInfo.getUserInfo(), request);
			if (redirectUrl != null) {
				return redirectUrl;
			}

			pageLogger(request, "/wechat/me", pageInfo);
		} catch (Exception e) {
			return errorPageRouter(e, "WechatWebController.me");
		}
		return "me";
	}

	/**
	 * 注册页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/me/register")
	public String register(HttpServletRequest request, HttpServletResponse response, Model model) {
		try {
			BasePageInfo pageInfo = wechatWebService.registerPage(request);
			model.addAttribute("eo", pageInfo.getUserInfo().getEncodeOpenid());
			model.addAttribute("user", pageInfo.getUserInfo().getUser());
			model.addAttribute("jsapi", pageInfo.getJsApiParam());

			pageLogger(request, "/wechat/register", pageInfo);
		} catch (Exception e) {
			return errorPageRouter(e, "WechatWebController.register");
		}
		return "user/register";
	}

	/**
	 * 注册 － ajax
	 * 
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping("/me/registe")
	public void registe(HttpServletRequest request, HttpServletResponse response, Model model) {
		AjaxResult ar = new AjaxResult();
		try {
			String eo = request.getParameter("eo");
			String mobile = request.getParameter("mobile");
			String code = request.getParameter("checkCode");
			BaseResult br = userService.register(mobile, code, eo);
			ar.setErrCode(br.getErrCode());
			ar.setErrMsg(br.getErrMsg());
		} catch (Exception e) {
			e.printStackTrace();
			ar.setErrCode("1001");
		}

		String json = JsonUtil.Object2JsonStr(ar);
		ResponseHandler.output(response, json);
	}

	/**
	 * 添加宝贝页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/item/addItem")
	public String addItem(HttpServletRequest request, HttpServletResponse response, Model model) {
		try {
			AddItemPageInfo addItemPageInfo = wechatWebService.addItemPage(request);
			model.addAttribute("jsapi", addItemPageInfo.getJsApiParam());
			model.addAttribute("itemTypes", addItemPageInfo.getItemTypes());

			String redirectUrl = registeredValidAndRedirect(addItemPageInfo.getUserInfo(), request);
			if (redirectUrl != null) {
				return redirectUrl;
			}

			pageLogger(request, "/wechat/item/addItem", addItemPageInfo);
		} catch (Exception e) {
			return errorPageRouter(e, "WechatWebController.addItem");
		}
		return "item/addItem";
	}

	/**
	 * 上传宝贝图片media_id-ajax <br>
	 * 目前本接口没有鉴权，存在一定安全隐患
	 * 
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping("/item/uploadMediaIds")
	public void uploadMediaIds(HttpServletRequest request, HttpServletResponse response, Model model) {
		AjaxResult ar = new AjaxResult();
		try {
			String mediaIds = request.getParameter("mediaIds");
			List<String> list = wechatNativeService.downloadItemImagesByMediaIds(mediaIds, request);
			ar.putData("urls", list);
			ar.setErrCode("0");
		} catch (Exception e) {
			ar.setErrCode("1001");
		}

		String json = JsonUtil.Object2JsonStr(ar);
		ResponseHandler.output(response, json);
	}

	/**
	 * 保存新宝贝-ajax
	 * 
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping("/item/saveItem")
	public void saveItem(HttpServletRequest request, HttpServletResponse response, Model model) {
		AjaxResult ar = new AjaxResult();
		try {
			String itemName = request.getParameter("itemName");
			String itemDesc = request.getParameter("itemDesc");
			String itemType = request.getParameter("itemType");
			String imgs = request.getParameter("imgs");
			String eo = request.getParameter("eo");

			if (StringUtil.isBlank(itemName) || StringUtil.isBlank(itemDesc) || StringUtil.isBlank(itemType)
					|| !StringUtil.isNumber(itemType) || StringUtil.isBlank(imgs) || StringUtil.isBlank(eo)) {
				ar.setErrCode("2001");
				ar.setErrMsg("信息不完整");
			} else {
				int itemTypeNum = 0;
				try {
					itemTypeNum = Integer.parseInt(itemType);
				} catch (Exception e) {
					ar.setErrCode("2002");
					ar.setErrMsg("信息不完整");
					String json = JsonUtil.Object2JsonStr(ar);
					ResponseHandler.output(response, json);
					return;
				}

				if (itemService.saveItem(eo, itemName, itemDesc, itemTypeNum, imgs)) {
					ar.setErrCode("0");
					ar.setErrMsg("保存成功");
				} else {
					ar.setErrCode("2003");
					ar.setErrMsg("信息不完整");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			ar.setErrCode("1001");
			ar.setErrMsg("信息不完整");
		}

		String json = JsonUtil.Object2JsonStr(ar);
		ResponseHandler.output(response, json);
	}

	/**
	 * 错误页面路由
	 * 
	 * @param e
	 * @return
	 */
	private String errorPageRouter(Exception e, String module) {
		// e.printStackTrace();

		if (e instanceof OAuthException) {
			logService.systemException("OAuth fail", module);
			return "error/403";
		}
		logService.systemException(e.getMessage(), module);
		return "error/500";
	}

	/**
	 * 页面浏览记录日志
	 * 
	 * @param request
	 * @param URI
	 * @param basePageInfo
	 */
	private void pageLogger(HttpServletRequest request, String URI, BasePageInfo basePageInfo) {
		String ip = null;
		try {
			ip = RequestHandler.getIpAddr(request);
		} catch (UnknownHostException e) {
			ip = e.getMessage();
		}
		int userId = 0;
		if (basePageInfo != null && basePageInfo.getUserInfo() != null && basePageInfo.getUserInfo().getUser() != null
				&& basePageInfo.getUserInfo().getUser().getUserId() != null) {
			userId = basePageInfo.getUserInfo().getUser().getUserId();
		}
		logService.pageLogger(URI, ip, userId);
	}

	/**
	 * 注册校验，并返回路径
	 * 
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	private String registeredValidAndRedirect(UserInfo userInfo, HttpServletRequest request) throws Exception {
		if (userInfo == null) {
			return "error/403";
		}

		if (userInfo.getUser().getMobile() == null || userInfo.getUser().getMobile().trim().equals("")) {
			String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath() + WechatConstant.SCHEMA_MARKET + "/";
			return "redirect:" + basePath + "me/register";
		}

		return null;
	}

}