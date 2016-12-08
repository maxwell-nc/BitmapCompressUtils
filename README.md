## Description
Compress a bitmap to traget size through scale and resample.
core:
sacle bitmap by calculate file size and target size ratio, if is not enough, reducing the sampling rate circularly.

## 说明
压缩图片到指定文件大小
核心：
先通过计算文件大小与目标大小的比例，缩放成合理的宽高，如果体积还不够小，则通过循环减少采样率来减少文件大小。