<?php
echo getcwd();
$new_sim = exec("env -u LD_LIBRARY_PATH ./feature-match /home/glyme/Downloads/snb.jpg /home/glyme/Zend/workspaces/DefaultWorkspace/image-detect/picstore/9.jpg 2>&1", $output, $return_val);
print_r($output);
?>