<?php
//
/**
 *  <p>判断图片类型是否合法</p>
 * @param $ext extension of file
 * @return boolean
 */
function is_image_validated ($ext)
{
    $ext_array = array(
        '.jpg',
        '.jpeg',
        '.gif',
        '.png'
    );
    $valiated = array_search($ext, $ext_array) != NULL;
    return $valiated;
}

// 把图片存储为jpeg格式
/**
 * @param image path $path
 * @return string
 */
function save_image_as_jpeg ($path)
{
    $ext = image_type_to_extension(exif_imagetype($path));
    if ($ext == '') {
        // 识别不了图片类型，可能不是图片
        header("HTTP/1.0 404 Bad Request");
        header("Status: 1304");
    }
    if (! is_image_validated($ext)) {
        // 不支持的图片格式
        header("HTTP/1.0 404 Bad Request");
        header("Status: 1301");
    }
    switch ($ext) {
        case '.jpeg':
            rename($path, $path . '.jpg');
            break;
        case '.gif':
            $img = imagecreatefromgif($path);
            imagejpeg($img, $path . '.jpg');
            imagedestroy($img);
            break;
        case '.png':
            $img = imagecreatefrompng($path);

            // convert transparent png to white-backgrounded jpeg
            $w = imagesx($img);
            $h = imagesy($img);
            $newimg = imagecreatetruecolor($w, $h);
         
            // Fill the new image with white background
            $bg = imagecolorallocate($newimg, 255, 255, 255);
            imagefill($newimg, 0, 0, $bg);
         
            // Copy original transparent image onto the new image
            imagecopy($newimg, $img, 0, 0, 0, 0, $w, $h);
            imagejpeg($newimg, $path . '.jpg');
            imagedestroy($img);
            imagedestroy($newimg);
            break;
    }
    return $path . '.jpg';
}

/**
 * @param image path $path
 *  缩放图片
 */
function fit_scale ($path)
{
    list ($src_w, $src_h) = getimagesize($path);
    
    if (($src_w < 800) && ($src_h) < 600) {
        return;
    }
    
    $new_w = 800;
    $new_h = $new_w / $src_w * $src_h;
    // 缩放
    $src = imagecreatefromjpeg($path);
    $target = imagecreatetruecolor($new_w, $new_h);
    imagecopyresampled($target, $src, 0, 0, 0, 0, $new_w, $new_h, $src_w, 
        $src_h);
    
    // 保存
    imagejpeg($target, $path);
    imagedestroy($target);
}

// Get all files with path $path
function get_file_list ($path)
{
    if (strrpos($path, DIRECTORY_SEPARATOR) !=
         (strlen($path) - strlen(DIRECTORY_SEPARATOR)))
        $path = $path . DIRECTORY_SEPARATOR;
    
    $fileArray[] = [];
    if (false != ($handle = opendir($path))) {
        $i = 0;
        while (false !== ($file = readdir($handle))) {
            // 去掉"“.”、“..”以及文件夹
            if ($file != "." && $file != ".." && ! is_dir($file)) {
                $fileArray[$i] =  $path . $file;
                $i ++;
            }
        }
        // 关闭句柄
        closedir($handle);
    }
    return $fileArray;
}

// 查询文件大小
function gethttpfilesize ($url)
{
    $ch = curl_init();
    
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_HEADER, 1);
    curl_setopt($ch, CURLOPT_NOBODY, 1);
    $okay = curl_exec($ch);
    curl_close($ch);
    
    $head = ob_get_contents();
    @ob_end_clean();
    $regex = '/Content-Length:\s?(\d+)/';
    $count = preg_match($regex, $head, $matches);
    // if there was a Content-Length field, its value
    // will now be in $matches[1]
    if (isset($matches[1])) {
        $size = $matches[1];
    } else {
        $size = - 1;
    }
    return $size;
}

// 下载文件
function httpcopy ($url, $file = "", $max_size = -1, $timeout = 60)
{
    $file = empty($file) ? pathinfo($url, PATHINFO_BASENAME) : $file;
    $dir = pathinfo($file, PATHINFO_DIRNAME);
    ! is_dir($dir) && @mkdir($dir, 0755, true);
    $url = str_replace(" ", "%20", $url);
    
    // 判断大小
    if ($max_size != - 1) {
        $size = gethttpfilesize($url);
        if ($size == - 1) {
            // 下载失败
            http_response_code(400);
            header("Status: 1302");
            return false;
        } else 
            if ($size > $max_size) {
                // 图片太大
                http_response_code(400);
                header("Status: 1303");
                return false;
            }
    }
    
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_TIMEOUT, $timeout);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
    $temp = curl_exec($ch);
    
    if (@file_put_contents($file, $temp) && ! curl_error($ch)) {
        curl_close($ch);
        return $file;
    } else {
        curl_close($ch);
        return false;
    }
}
?>