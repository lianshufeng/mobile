<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>资源发布</title>
</head>
<body>

	<form action="<%=basePath%>HotUpdate/publish.json"  method="POST" enctype="multipart/form-data">
		<p>
			<span>资源ID</span> <input type="text" value="phone" name="appId" />
		</p>
		<p>
			<span>上传资源(Zip)</span> <input type="file" name="resFile" />
		</p>
		<p>
			<input type="submit" value="提交" /> <input type="reset" value="重置" />
		</p>
	</form>

</body>
</html>