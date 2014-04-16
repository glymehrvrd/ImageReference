<?php
// 判断参数是否正确
if ((! isset($_GET['url']) && ! isset($_FILES['img'])) ||
     (isset($_GET['url']) && isset($_FILES['img']))) {
    // 1004 400 MISSION_ARGUMENTS
    header("HTTP/1.0 404 Bad Request");
    header("Status: 1004");
    return;
}

require_once 'utils.php';
$path = 'imgtmp';
global $imgname;

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
    move_uploaded_file($_FILES['img']['tmp_name'], $path);
    $imgname = save_image_as_jpeg($path);
    fit_scale($imgname);
}

require_once 'detectproc.php';
$best_match = get_best_match($imgname);

require_once 'sql_connect.php';
get_relative_info($best_match);

if (isset($_GET['dbg'])||isset($_POST['dbg'])) {
    // 显示图片
    echo '<div style="float:left">';
    echo '<img src="' . $imgname . '">';
    echo '</div>';
    
    echo '<div style="float:right">';
    echo '<img src="' . $best_match . '">';
    echo '</div>';
}
?>