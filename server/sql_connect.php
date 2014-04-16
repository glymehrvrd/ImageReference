<?php

function get_relative_info($img_path)
{
    $link = mysql_connect('localhost', 'root', '123') or
         die("Could not connect: " . mysql_error());
    mysql_select_db("image-detector") or die("Could not select database");
    mysql_query('SET NAMES UTF8');

    $img_name = substr($img_path, strrpos($img_path, '/') + 1);
    $rs = mysql_query("select * from imagedata where `img`='$img_name'", $link);
    if ($rs) {
        while ($row = mysql_fetch_row($rs)) {
            $fdata = base64_encode(file_get_contents($img_path));
            $arr = array(
                'url' => $row[1],
                'describe' => $row[3],
                'session_id' => session_id(),
                'img' => $fdata
            );
            echo json_encode($arr);
            //var_dump($arr);
        }
        mysql_free_result($rs);
    }
    mysql_close($link);
}
?>