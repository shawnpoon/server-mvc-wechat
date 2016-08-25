<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	String path = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head lang="zh">
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,user-scalable=no" />
<meta name="format-detection" content="telephone=no">
<title>正佳分享集市</title>
<link rel="stylesheet" href="<%=path%>/include/css/ta.css">
</head>
<body>

	<div class="container">

		<div class="cloud"></div>

		<div class="me-head">
			<div class="head-img">
				<img src="${user.headImgUrl }">
			</div>
			<div class="nickname">${user.nickname}</div>
			<div class="sex1"></div>

			<div class="info-view">
				<div class="integral-icon"></div>
				<div class="integral">12345</div>
				<div class="line"></div>
				<div class="like-icon"></div>
				<div class="like">${user.likeNum}</div>
				<div class="line"></div>
				<div class="v-icon"></div>
				<div class="v">${user.exchangeNum}</div>
			</div>
		</div>

		<div class="itemList">
			<c:forEach items="${items}" var="item">
				<div class="item" onclick="evens.onItemClick(${item.itemId})">
					<div class="info">
						<div class="time">${item.displayTime }</div>
						<div class="description">${item.description}</div>
						<div class="img-view">
							<img src="${item.firstItemCenterImgUrl }">
						</div>
						<div class="wishBar">
							<div class="tag"></div>
							<div class="wishItem">${item.wishItem}</div>
						</div>
					</div>

					<div class="foot-bar">
						<div class="text">${item.likeNum}</div>
						<div class="likeNumTag"></div>
					</div>

					<div class="itemType">${item.itemTypeName }</div>
				</div>
			</c:forEach>
		</div>
	</div>


	<input type="hidden" id="appid" value="${jsapi.appid}">
	<input type="hidden" id="nonceStr" value="${jsapi.nonceStr}">
	<input type="hidden" id="timeStamp" value="${jsapi.timeStamp}">
	<input type="hidden" id="signature" value="${jsapi.signature}">
	<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>

	<script src="<%=path%>/include/jquery/jquery.min.js"></script>
	<script src="<%=path%>/include/core/core.js"></script>

	<script>
		var jsapiparam = {};
		jsapiparam.appid = document.getElementById("appid").value;
		jsapiparam.nonceStr = document.getElementById("nonceStr").value;
		jsapiparam.timeStamp = document.getElementById("timeStamp").value;
		jsapiparam.signature = document.getElementById("signature").value;

		jsapiparam.isWxJsApiReady = false;

		wx.config({
			debug : false,
			appId : jsapiparam.appid,
			timestamp : jsapiparam.timeStamp,
			nonceStr : jsapiparam.nonceStr,
			signature : jsapiparam.signature,
			jsApiList : [ "hideOptionMenu" ]
		});

		wx.ready(function() {
			jsapiparam.isWxJsApiReady = true;
			wx.hideOptionMenu();
		});

		wx.error(function(res) {
			wx.hideOptionMenu();
		});

		var evens = {};

		$(function() {

			evens.onItemClick = function(id) {
				var eo = Core.getQueryString("eo");
				location.href = "item/item?ii=" + id + "&eo=" + eo;
			}
		});
	</script>

</body>
</html>