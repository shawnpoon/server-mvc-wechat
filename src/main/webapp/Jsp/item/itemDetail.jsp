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
<title>正佳分享集市</title>
<link rel="stylesheet" href="<%=path%>/include/css/itemDetail.css">
</head>
<body>
	<div class="container" id="container">

		<div class="main-view">

			<div class="head-view">
				<div class="head-img">
					<img src="${owner.headImgUrl }">
				</div>
				<div class="info-view">
					<div class="line">
						<div class="nickname">${owner.nickname }</div>
						<div class="state">${item.lockStatus == 'exchanged' ? '已交换':'交换中' }</div>
					</div>
					<div class="line">
						<div class="time">${item.displayTime }</div>
						<div class="itemType">${item.itemTypeName }</div>
					</div>
				</div>
			</div>

			<div class="content-view">
				<div class="itemName">${item.name}</div>
				<div class="itemDescription">${item.description}</div>
				<c:forEach items="${itemImgList }" var="imgUrl">
					<div class="itemImg">
						<img src="${imgUrl }">
					</div>
				</c:forEach>

				<div class="wish">
					<div class="icon"></div>
					<div class="text">${item.wishItem }</div>
				</div>

				<div class="like-view" id="likeBtn">
					<div class="icon"></div>
					<div class="text">${item.likeNum }</div>
				</div>
			</div>

			<div class="comment-view">
				<div class="header">
					<div class="count">
						<div class="icon"></div>
						<div class="text">123123</div>
					</div>
				</div>
				<div class="comments">
					<div class="comment">
						<div class="head-img">
							<img
								src="http://www.joosure.com/wechatTest/head_img/20166/15/BXBVz7YlhAqRHq4CJ0jvEAJtRDRPcPcq.jpg">
						</div>
						<div class="info">
							<div class="line">
								<span>nickname</span> &nbsp;&nbsp;<span>time</span>
							</div>
							<div class="content">123123123123</div>
						</div>
					</div>
				</div>
				<div class="loading">加载中</div>
			</div>

			<div class="foot-bar">
				<div class="btns">
					<div class="col-33">
						<div class="submitBtnView">
							<a class="btn" href="javascript:void(0);">写评论</a>
						</div>
					</div>
					<div class="col-66">
						<c:if
							test="${user.user.userId != owner.userId and item.lockStatus != 'exchanged'}">
							<div class="submitBtnView">
								<a class="btn" href="${toExchangeUrl}">我要交换</a>
							</div>
						</c:if>
					</div>
				</div>

				<div class="ps-text">宝贝发布需后台审核通过后才可见</div>
			</div>
		</div>

		<div class="comment-view">
			
		</div>
	</div>


	<input type="hidden" id="ii" value="${item.itemId}">

	<input type="hidden" id="appid" value="${jsapi.appid}">
	<input type="hidden" id="nonceStr" value="${jsapi.nonceStr}">
	<input type="hidden" id="timeStamp" value="${jsapi.timeStamp}">
	<input type="hidden" id="signature" value="${jsapi.signature}">

	<input type="hidden" id="shareTitle" value="${share.title}">
	<input type="hidden" id="shareImg" value="${share.imgUrl}">
	<input type="hidden" id="shareLink" value="${share.link}">
	<input type="hidden" id="shareDesc" value="${share.desc}">

	<script src="<%=path%>/include/jquery/jquery.min.js"></script>
	<script src="<%=path%>/include/core/core.js"></script>
	<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
	<script src="<%=path%>/include/page/itemDetail.js"></script>
</body>
</html>