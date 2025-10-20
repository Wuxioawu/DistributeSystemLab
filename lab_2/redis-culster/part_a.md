


for port in 7001 7002 7003 7004 7005 7006; do
cat > ${port}/redis.conf <<EOF
port ${port}
bind 0.0.0.0
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
appendonly yes
protected-mode no
cluster-announce-ip 127.0.0.1
cluster-announce-port ${port}
cluster-announce-bus-port 1${port}
EOF
done



docker-compose exec redis-7001 redis-cli --cluster create \
redis-7001:7001 redis-7002:7002 redis-7003:7003 \
redis-7004:7004 redis-7005:7005 redis-7006:7006 \
--cluster-replicas 1


docker-compose exec redis-7001 redis-cli -c -p 7001 cluster info