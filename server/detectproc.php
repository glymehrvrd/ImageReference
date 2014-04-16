<?php
require_once 'utils.php';

/**
 * @param source image to be compared $src_img
 * @return string
 */
function get_best_match($src_img)
{
    $filelist = get_file_list('./picstore');
    $sim = 0;
    $best_match_file = "";
    foreach ($filelist as $f) {
        $new_sim = exec("./feature-match " . $f . " " . $src_img);
        $new_sim = intval($new_sim);

        if ($new_sim > $sim) {
            $best_match_file = $f;
            $sim = $new_sim;
        }
    }

    return $best_match_file;
}
?>