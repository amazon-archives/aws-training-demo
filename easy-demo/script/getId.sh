export INSTANCE_ID=$(curl -s http://169.254.169.254/latest/meta-data/instance-id)
echo $INSTANCE_ID