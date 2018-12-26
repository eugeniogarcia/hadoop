#! /bin/bash

#hadoop pre-reqs
apt-get update
apt-get install -y ssh rsync

#hadoop 2.7.2
wget --quiet http://mirrors.ukfast.co.uk/sites/ftp.apache.org/hadoop/common/hadoop-2.7.2/hadoop-2.7.2.tar.gz
tar xzf hadoop-2.7.2.tar.gz -C /opt/
ln -s /opt/hadoop-2.7.2 /opt/hadoop
rm hadoop-2.7.2.tar.gz