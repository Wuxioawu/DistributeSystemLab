#!/bin/bash

# List of Redis ports for cluster nodes
ports=(7001 7002 7003 7004 7005 7006)

# Loop through each port and create configuration
for port in "${ports[@]}"; do
  # Create folder for this node
  mkdir -p ${port}

  # Write redis.conf file
  cat > ${port}/redis.conf <<EOF
port ${port}
bind 0.0.0.0
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
cluster-announce-ip redis-${port}
cluster-announce-port ${port}
cluster-announce-bus-port 1${port}
appendonly yes
protected-mode no
EOF

  echo "Created configuration for node ${port}"
done

echo "All Redis configurations have been created."


