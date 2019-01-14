<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@include file="common/tag.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <title>上传ftp图片</title>
    <%@include file="common/head.jsp" %>
        $(function(){
            $("#doUpload").on('click',function(event){
                var formData = new FormData();
                var jtbm = $("#hidden_jtbm").val();
                var zjlx = $("#select_fj").val();
                <!--此部分若不懂，往下看解释-->
                formData.append("jtbm",jtbm);
                formData.append("zjlx",zjlx);
                formData.append("file",$("#zjfile")[0].files[0]);

                $.ajax({
                    url : rootUri + 'ftp/upload',//你自己的url地址
                    type : 'post',
                    data : formData,
                    cache : false,
                    processData : false,
                    contentType : false,
                    async : false,
                    success : function(d) {
                        alert("上传成功");
                    }
                })
            })
        })

</head>
<body>
<form id="form_fj" enctype="multipart/form-data">
    <div><!--此处上传一个id值 -->
        <input type="text" id="hidden_jtbm"/>
    </div>
    <div id="divselect"> <!-- 该下拉框可以选择两种类型照片上传，根据数据库表业务逻辑来的，不必深究-->
        选择身份
        <select id="select_fj">
            <option value ="1">小哥哥照片</option>
            <option value ="2">小姐姐照片</option>
        </select>
    </div>
    <div id="filePicker" class="uploader-list"><!--选择需要上传的图片 -->
        <input type="file" id="zjfile" name="zjfj" multiple="multiple"/>
    </div>
    <div><input type="button" value="上传" id="doUpload" /></div>
    </form>
</body>
</html>

