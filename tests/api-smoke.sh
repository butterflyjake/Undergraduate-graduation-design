#!/bin/bash
# 高校AED系统 后端接口全流程联调脚本
# 前置: 后端已启动(8080), 已导入 init.sql + demo_data.sql
# 用法: bash tests/api-smoke.sh
BASE=http://localhost:8080/api
J='Content-Type: application/json'
login() { curl -s -X POST $BASE/auth/login -H "$J" -d "{\"username\":\"$1\",\"password\":\"$2\"}" | sed 's/.*"token":"\([^"]*\)".*/\1/'; }

TOKEN=$(login admin 123456)
WTOKEN=$(login wangfang 123456)
ZTOKEN=$(login zhangwei 123456)
LTOKEN=$(login liming 123456)

echo "========== 1. 生成今日任务(今日已有任务, 应去重无新增) =========="
curl -s -X POST $BASE/inspection/task/generate -H "Authorization: Bearer $TOKEN" -H "$J" -d '{"planDate":"2026-10-28"}'; echo

echo "========== 2. wangfang 提交正常巡检 taskId=16 (AED-003) =========="
curl -s -X POST $BASE/inspection/record/submit -H "Authorization: Bearer $WTOKEN" -H "$J" --data-binary @- <<'EOF'
{"taskId":16,"summary":"设备运行正常, 各项检查均通过","items":[{"itemName":"主机外观完好无破损","itemResult":1,"remark":""},{"itemName":"电源指示灯显示正常","itemResult":1,"remark":""},{"itemName":"电极片在有效期内","itemResult":1,"remark":""},{"itemName":"电池电量充足","itemResult":1,"remark":""},{"itemName":"机柜箱体完好","itemResult":1,"remark":""},{"itemName":"设备周边无障碍物","itemResult":1,"remark":""}]}
EOF
echo

echo "========== 3. wangfang 提交异常巡检 taskId=19 (AED-010 电极片临期) =========="
curl -s -X POST $BASE/inspection/record/submit -H "Authorization: Bearer $WTOKEN" -H "$J" --data-binary @- <<'EOF'
{"taskId":19,"summary":"电极片即将到期, 需安排更换","items":[{"itemName":"主机外观完好无破损","itemResult":1,"remark":""},{"itemName":"电源指示灯显示正常","itemResult":1,"remark":""},{"itemName":"电极片在有效期内","itemResult":2,"remark":"电极片有效期剩余不足30天"},{"itemName":"电池电量充足","itemResult":1,"remark":""},{"itemName":"机柜箱体完好","itemResult":1,"remark":""},{"itemName":"设备周边无障碍物","itemResult":1,"remark":""}]}
EOF
echo

echo "========== 4. 验证自动建单: 最新工单应为巡检异常/AED-010/王芳 =========="
curl -s "$BASE/repair/page?pageNum=1&pageSize=1" -H "Authorization: Bearer $TOKEN" | head -c 700; echo

echo "========== 5. zhangwei 接单(新工单id=5) =========="
curl -s -X POST $BASE/repair/5/accept -H "Authorization: Bearer $ZTOKEN"; echo

echo "========== 6. zhangwei 完成工单5 =========="
curl -s -X POST $BASE/repair/5/complete -H "Authorization: Bearer $ZTOKEN" -H "$J" --data-binary @- <<'EOF'
{"repairDesc":"已更换全新电极片一组, 设备自检正常","repairCost":350.00}
EOF
echo

echo "========== 7. 工单5最终状态 =========="
curl -s $BASE/repair/5 -H "Authorization: Bearer $TOKEN" | head -c 600; echo

echo "========== 8. 配件更换联动: zhangwei 为 AED-003 登记新电极片 =========="
curl -s -X POST $BASE/part -H "Authorization: Bearer $ZTOKEN" -H "$J" --data-binary @- <<'EOF'
{"deviceId":3,"partType":1,"batchNo":"EP20261028-01","installDate":"2026-10-28","expireDate":"2028-10-28","remark":"按工单更换"}
EOF
echo
echo "-- 旧电极片(part6)应变为已更换, 其未处理预警(预警1)应自动关闭 --"
curl -s "$BASE/part/page?pageNum=1&pageSize=5&deviceId=3" -H "Authorization: Bearer $TOKEN" | head -c 500; echo
curl -s $BASE/warning/1/handle -X POST -H "Authorization: Bearer $TOKEN" > /dev/null 2>&1
curl -s "$BASE/warning/page?pageNum=1&pageSize=8" -H "Authorization: Bearer $TOKEN" | head -c 900; echo

echo "========== 9. 预警处理: zhangwei 处理预警5(AED-006 电极片已过期) =========="
curl -s -X POST $BASE/warning/5/handle -H "Authorization: Bearer $ZTOKEN" -H "$J" --data-binary @- <<'EOF'
{"handleDesc":"已联系供应商采购新电极片, 到货后立即更换"}
EOF
echo

echo "========== 10. zhangwei 手动报修 AED-012 =========="
curl -s -X POST $BASE/repair -H "Authorization: Bearer $ZTOKEN" -H "$J" --data-binary @- <<'EOF'
{"deviceId":12,"faultDesc":"电池仓卡扣断裂, 电池无法固定"}
EOF
echo

echo "========== 11. liming 驳回新工单(应id=6) =========="
curl -s -X POST $BASE/repair/6/reject -H "Authorization: Bearer $LTOKEN" -H "$J" --data-binary @- <<'EOF'
{"rejectReason":"该设备已停用, 走报废流程处理"}
EOF
echo

echo "========== 12. 越权防护: wangfang 访问陈静的任务详情(taskId=18) 应403 =========="
curl -s $BASE/inspection/task/18 -H "Authorization: Bearer $WTOKEN"; echo
echo "-- admin 访问同一任务应200 --"
curl -s $BASE/inspection/task/18 -H "Authorization: Bearer $TOKEN" | head -c 300; echo

echo "========== 13. 用户闭环: 新增zhangsan → 删除 → 重建同名 =========="
curl -s -X POST $BASE/user -H "Authorization: Bearer $TOKEN" -H "$J" --data-binary @- <<'EOF'
{"username":"zhangsan","password":"123456","realName":"张三","phone":"13800000099","roleId":3,"status":1}
EOF
echo
curl -s -X DELETE $BASE/user/7 -H "Authorization: Bearer $TOKEN"; echo
curl -s -X POST $BASE/user -H "Authorization: Bearer $TOKEN" -H "$J" --data-binary @- <<'EOF'
{"username":"zhangsan","password":"123456","realName":"张三","phone":"13800000099","roleId":3,"status":1}
EOF
echo
curl -s -X DELETE $BASE/user/8 -H "Authorization: Bearer $TOKEN"; echo

echo "========== 14. 设备闭环: 删AED-011 → 重建 → 删重复编号尝试 =========="
curl -s -X DELETE $BASE/device/11 -H "Authorization: Bearer $TOKEN"; echo
curl -s -X POST $BASE/device -H "Authorization: Bearer $TOKEN" -H "$J" --data-binary @- <<'EOF'
{"deviceCode":"AED-011","deviceName":"鱼跃普美康 HeartSave","brand":"鱼跃","buildingId":1,"floor":"5F","positionDesc":"教师休息区门口","status":0}
EOF
echo
curl -s -X POST $BASE/device -H "Authorization: Bearer $TOKEN" -H "$J" --data-binary @- <<'EOF'
{"deviceCode":"AED-002","deviceName":"测试重复编号","buildingId":1,"status":0}
EOF
echo
curl -s -X DELETE $BASE/device/13 -H "Authorization: Bearer $TOKEN"; echo

echo "========== 15. 修改密码闭环: zhangwei 123456→abc12345→改回 =========="
curl -s -X PUT $BASE/auth/password -H "Authorization: Bearer $ZTOKEN" -H "$J" -d '{"oldPassword":"123456","newPassword":"abc12345"}'; echo
echo "-- 旧密码登录应失败 --"
curl -s -X POST $BASE/auth/login -H "$J" -d '{"username":"zhangwei","password":"123456"}' | head -c 120; echo
echo "-- 新密码登录应成功 --"
NT=$(login zhangwei abc12345); echo "新token长度: ${#NT}"
echo "-- 改回123456 --"
curl -s -X PUT $BASE/auth/password -H "Authorization: Bearer $NT" -H "$J" -d '{"oldPassword":"abc12345","newPassword":"123456"}'; echo

echo "========== 16. 最终仪表盘 =========="
curl -s $BASE/dashboard/overview -H "Authorization: Bearer $TOKEN"; echo
echo "========== 全流程联调完成 =========="
