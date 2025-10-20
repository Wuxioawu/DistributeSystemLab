# recreate the redis config files and restart the cluster

cd ~/IdeaProjects/socket_lab/lab_2/redis-culster
docker-compose down
rm -rf 7001 7002 7003 7004 7005 7006

for port in 7001 7002 7003 7004 7005 7006; do
mkdir -p ${port}
cat > ${port}/redis.conf <<EOF
port ${port}
bind 0.0.0.0
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
appendonly yes
protected-mode no
EOF
done

# execute the docker compose

docker-compose up -d
sleep 20

# create the redis cluster

docker-compose exec redis-7001 redis-cli --cluster create \
redis-7001:7001 redis-7002:7002 redis-7003:7003 \
redis-7004:7004 redis-7005:7005 redis-7006:7006 \
--cluster-replicas 1

# modify the config epoch and announce ip

docker-compose exec redis-7001 redis-cli -p 7001 CLUSTER SET-CONFIG-EPOCH 1
docker-compose exec redis-7001 redis-cli -p 7001 CONFIG SET cluster-announce-ip 172.18.195.82
docker-compose exec redis-7002 redis-cli -p 7002 CONFIG SET cluster-announce-ip 172.18.195.82
docker-compose exec redis-7003 redis-cli -p 7003 CONFIG SET cluster-announce-ip 172.18.195.82
docker-compose exec redis-7004 redis-cli -p 7004 CONFIG SET cluster-announce-ip 172.18.195.82
docker-compose exec redis-7005 redis-cli -p 7005 CONFIG SET cluster-announce-ip 172.18.195.82
docker-compose exec redis-7006 redis-cli -p 7006 CONFIG SET cluster-announce-ip 172.18.195.82

# check the cluster nodes

docker-compose exec redis-7001 redis-cli -c -p 7001 cluster nodes


# The nodes distribution
Master 7001 (0-5460)  ← Slave 7005
Master 7002 (5461-10922) ← Slave 7006
Master 7003 (10923-16383) ← Slave 7004