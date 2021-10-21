# Fnd Route Server

```bash
# 打包
gradle :findroute-server:build

# 运行
java -jar \
  -Dfile.encoding=utf-8 \
  -Djava.awt.headless=true \
  -Duser.timezone=GMT+08 \
  findroute-server-1.0.jar \
  findroute.dev.properties &
```
