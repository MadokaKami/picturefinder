<!doctype html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta content='initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width' name='viewport'>
    <title>重复图像检测报告</title>
    <style type="text/css">
        *{margin: 0;padding: 0;}
        .srcImageDiv{
            width:610px;
            height: 470px;
            text-align: center;
            border:1px solid #9bdf70;
            background:#f0fbeb;
            color: darkgoldenrod;
            margin: 10px 0 0 200px;
        }
        .srcImageDiv img {
            max-width: 600px;
            max-height: 420px;
            vertical-align: middle;
            width: auto;
            height: auto;
            margin-top: 5px;
        }
        .imageDiv{
            width:510px;
            height: 450px;
            display: table-cell;
            vertical-align: middle;
            text-align: center;
            border:1px solid #a5b6c8;
            background:#eef3f7;
            color: darkgoldenrod;
        }
        .imageDiv img {
            max-width: 500px;
            max-height: 350px;
            width: auto;
            height: auto;
            vertical-align:middle;
        }
        body{
            text-align:center;
        }
        .htmleaf-container{
            margin:auto;
            width: auto;
            vertical-align:middle;
        }
        .showImageTable{
            margin:auto;
        }
        .showImageTr{
            display: block;
            margin-bottom: 5px;
        }
    </style>
</head>
<body>
<div class="htmleaf-container">
    <#list sameImageInfoList as sameImageInfo>
    <fieldset>
        <legend>图片${sameImageInfo_index + 1}</legend>
            <div class="srcImageDiv">
                <img src="file:///${sameImageInfo.imageInfo.filePath}">
                <div>
                    <span>图像地址 --> ${sameImageInfo.imageInfo.filePath}</span>
                </div>
            </div>

            <table class="showImageTable">
        <#list sameImageInfo.contrastImageInfoList as contrastImageInfo>
           <#if contrastImageInfo_index % 3 == 0>
                <tr class="showImageTr" inlist="${contrastImageInfo_index}">
           </#if>
                    <td>
                        <div class="imageDiv">
                            <img src="file://${contrastImageInfo.filePath}">
                            <br>
                            <span>图像地址 --> ${contrastImageInfo.filePath}</span>
                            <br>
                            <span>重复百分比:${contrastImageInfo.samePixelPercent}/span>
                        </div>
                    </td>
            <#if (contrastImageInfo_index + 1) % 3 == 0 || !contrastImageInfo_has_next>
                </tr>
           </#if>
        </#list>
            </table>
    </fieldset>
    </#list>
</div>

</body>
</html>