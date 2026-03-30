# 自动化测试脚本

# 时间限制
timelimit=$1

#工作空间
basedir="/workspace"

#输出结果路径
result=${basedir}/task/result
mkdir -p $result

# 测试数据路径
DataFilePath=${basedir}/data

for i in $( ls "$DataFilePath" ); do

        # 输入文件
        input_file=${DataFilePath}/${i}/input.txt

        # 输出文件目录
        output_dir=${result}/${i}
        mkdir -p ${output_dir}

        # 执行每个测试数据
        /usr/bin/time -o ${output_dir}/time_log.txt -v timeout -k 1 "${timelimit}" \
                  java -cp "${basedir}/task" Main < ${input_file} \
                > ${output_dir}/success.txt 2> ${output_dir}/error.txt
done
