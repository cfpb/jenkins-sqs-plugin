# SQS plugin for Jenkins [![Build Status](https://img.shields.io/travis/cfpb/jenkins-sqs-plugin.svg?maxAge=2592000&style=flat-square)](https://travis-ci.org/cfpb/jenkins-sqs-plugin)

Based on the [Mattermost plugin](https://github.com/jenkinsci/mattermost-plugin/) which was a fork of the [Slack plugin](https://github.com/jenkinsci/slack-plugin/) which was a fork of the [HipChat plugin](https://github.com/jlewallen/jenkins-hipchat-plugin) which was a fork of the Campfire plugin.

Includes [Jenkins Pipeline](https://github.com/jenkinsci/workflow-plugin) support:

```
sqsSend color: 'good', message: 'Message from Jenkins Pipeline'
```

# Jenkins Instructions

1. Create an [AWS SQS](https://aws.amazon.com/sqs/) queue.
1. Install this plugin on your Jenkins server.
1. Configure this plugin.
1. **Add it as a Post-build action** in your Jenkins job.

It will send a message to your specified AWS SQS queue in the following format:

```
{
    "QueueUrl": "https://queue.amazonaws.com/1234567890/your-queue",
    "MessageBody": "Configurable job message",
    "MessageAttributes": {
        "user": {
            "DataType": "String",
            "StringValue": "Jenkins"
        },
        "room": {
            "DataType": "String",
            "StringValue": "#off-topic"
        }
    },
    "DelaySeconds": 0
}
```

You can then read from your SQS queue and route messages accordingly.

# Developer instructions

Install Maven and JDK.

Run unit tests

    mvn test

Create an HPI file to install in Jenkins (HPI file will be in `target/sqs.hpi`).

    mvn package
