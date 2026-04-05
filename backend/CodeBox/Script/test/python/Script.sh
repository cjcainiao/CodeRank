# 测试数据(测试沙箱)
basedir="/workspace"

#输出结果路径
result=${basedir}/task/result
mkdir -p $result

# 测试数据路径
DataFilePath=${basedir}/task

/usr/bin/time -o ${result}/time_log.txt -v timeout -k 1 1 \
 python3 "${DataFilePath}/Main.py" < "${DataFilePath}/input.txt" \
 > ${result}/success.txt 2> ${result}/error.txt