#!/bin/bash
ssh -i /home/sunweekstar/id_rsa centos@10.77.70.173 "rm -rf /home/centos/sunzhouxing/workflow/channel/channelConfig"
ssh -i /home/sunweekstar/id_rsa centos@10.77.70.122 "rm -rf /home/centos/sunzhouxing/workflow/channel/channelConfig"
ssh -i /home/sunweekstar/id_rsa centos@10.77.70.123 "rm -rf /home/centos/sunzhouxing/workflow/channel/channelConfig"
ssh -i /home/sunweekstar/id_rsa centos@10.77.70.124 "rm -rf /home/centos/sunzhouxing/workflow/channel/channelConfig"
ssh -i /home/sunweekstar/id_rsa centos@10.77.70.82 "rm -rf /home/centos/sunzhouxing/workflow/channel/channelConfig"
ssh -i /home/sunweekstar/id_rsa centos@10.77.70.83 "rm -rf /home/centos/sunzhouxing/workflow/channel/channelConfig"
ssh -i /home/sunweekstar/id_rsa centos@10.77.70.84 "rm -rf /home/centos/sunzhouxing/workflow/channel/channelConfig"
ssh -i /home/sunweekstar/id_rsa centos@10.77.70.183 "rm -rf /home/centos/sunzhouxing/workflow/channel/channelConfig"
ssh -i /home/sunweekstar/id_rsa centos@10.77.70.179 "rm -rf /home/centos/sunzhouxing/workflow/channel/channelConfig"
ssh -i /home/sunweekstar/id_rsa centos@10.77.70.182 "rm -rf /home/centos/sunzhouxing/workflow/channel/channelConfig"

scp -i /home/sunweekstar/id_rsa -r channelConfig centos@10.77.70.123:/home/centos/sunzhouxing/workflow/channel && scp -i /home/sunweekstar/id_rsa -r channelConfig centos@10.77.70.122:/home/centos/sunzhouxing/workflow/channel && scp -i /home/sunweekstar/id_rsa -r channelConfig centos@10.77.70.173:/home/centos/sunzhouxing/workflow/channel && scp -i /home/sunweekstar/id_rsa -r channelConfig centos@10.77.70.124:/home/centos/sunzhouxing/workflow/channel && scp -i /home/sunweekstar/id_rsa -r channelConfig centos@10.77.70.121:/home/centos/sunzhouxing/workflow/channel && scp -i /home/sunweekstar/id_rsa -r channelConfig centos@10.77.70.82:/home/centos/sunzhouxing/workflow/channel && scp -i /home/sunweekstar/id_rsa -r channelConfig centos@10.77.70.83:/home/centos/sunzhouxing/workflow/channel && scp -i /home/sunweekstar/id_rsa -r channelConfig centos@10.77.70.84:/home/centos/sunzhouxing/workflow/channel && scp -i /home/sunweekstar/id_rsa -r channelConfig centos@10.77.70.183:/home/centos/sunzhouxing/workflow/channel && scp -i /home/sunweekstar/id_rsa -r channelConfig centos@10.77.70.179:/home/centos/sunzhouxing/workflow/channel && scp -i /home/sunweekstar/id_rsa -r channelConfig centos@10.77.70.182:/home/centos/sunzhouxing/workflow/channel
