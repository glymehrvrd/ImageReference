<?php

// 判断参数是否正确
if ((! isset($_GET['url']) && file_get_contents('php://input') == "") ||
     (isset($_GET['url']) && file_get_contents('php://input') != "")) {
    // 1004 400 MISSION_ARGUMENTS
    header("HTTP/1.0 404 Bad Request");
    header("Status: 1004");
    return;
}

require_once 'utils.php';
$path = 'imgtmp';

// 上传的是图片的网址
if (isset($_GET['url'])) {
    // download image from web, 3MB for max size
    if (httpcopy($_GET['url'], $path, 3145728)) {
        // 获取完整图片名（含扩展名）
        $imgname = save_image_as_jpeg($path);
        fit_scale($imgname);
    }
} else {
    // 以post方式上传图片
    copy('php://input', $path);
    $imgname = save_image_as_jpeg($path);
    fit_scale($imgname);
}

require_once 'detectproc.php';
$best_match = get_best_match($imgname);

require_once 'sql_connect.php';
get_relative_info($best_match);

// 显示图片
$imghtml = isset($imgname) ? '<img style="BORDER-RIGHT: #990000 3px dashed; BORDER-TOP: #990000 3px dashed; BORDER-LEFT: #990000 3px dashed; BORDER-BOTTOM: #990000 3px dashed" src="' .
     $imgname . '">' : '';
echo $imghtml;
?>