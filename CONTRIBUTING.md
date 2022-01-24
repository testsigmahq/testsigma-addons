# Contributing to Testsigma Addons
Testsigma welcomes contributions from everyone. If you feel insecure about how to start contributing, feel free to ask us on our [Discord channel](https://discord.gg/invite/5caWS7R6QX) in the #contributors channel.

Add-ons extend the test automation capabilities of Testsigma. Learn more about add-ons [here](https://testsigma.com/docs/addons/what-is-an-addon/). Check out existing add-ons [here](https://testsigma.com/addons).

## Code of conduct
Read our [Code of Conduct](CODE_OF_CONDUCT.md) before contributing
 
## Good First add-ons

We appreciate first-time contributors and we are happy to assist you in getting started. In case of questions, just [reach out to us](https://discord.gg/invite/5caWS7R6QX)!

If you are not sure what add-ons to build checkout the good first add-ons list [here](https://github.com/testsigmahq/testsigma-addons/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+addon%22).  If you have interesting ideas to create add-ons, create a new addon request [here](https://github.com/testsigmahq/testsigma-addons/issues/new). 
 
### Set up your branch to build add-ons

We use [Github Flow](https://guides.github.com/introduction/flow/index.html), so all code changes happen through pull requests. [Learn more](https://blog.scottlowe.org/2015/01/27/using-fork-branch-git-workflow/).

1. Please make sure there is an issue associated with the work that you're doing. If it doesn’t exist, create an issue.
2. If you're working on an issue, please comment that you are doing so to prevent duplicate work by others also.
3. Fork the repo and create a new branch from the main branch.
4. Please name the branch as issue-**[issue-number]-[issue-name(optional)] or new-addon-[feature-number]–[feature-name(optional)]**. For example, if you are fixing Issue #205 name your branch as **issue-205 or issue-205-selectbox-handling-changes**
5. Please add tests for your changes.
6. Squash your commits and refer to the issue using `Fix #<issue-no>` in the commit message, at the start.
7. Rebase master with your branch and push your changes.
 
### 🏡 Setup for local development
Refer to [this](https://testsigma.com/docs/contributing/setup-dev-environment-add-ons) document to learn how to set up a dev environment.


## Committing code

The main branch(protected)contains the code that is tested and released. 
Pull requests should be made against the **main** branch. **main** contains all of the new features and fixes that are under testing and ready to go out in the next release.

Once you are done with the code changes on your local machine, follow the below steps to commit.

### Commit & Create Pull Requests 

1. Please make sure there is an issue associated with the work that you're doing. If it doesn’t exist, [create an issue](https://github.com/testsigmahq/testsigma-addons/issues/new/choose).
2. Squash your commits and refer to the issue using `Fix #<issue-no>` in the commit message, at the start.
3. Rebase master with your branch and push your changes.
4. Once you are confident in your code changes, create a pull request in your fork to the main branch in the testsigmahq/testsigma-add-ons base repository.
5. Link the issue of the base repository in your Pull request description. [Guide](https://docs.github.com/en/free-pro-team@latest/github/managing-your-work-on-github/linking-a-pull-request-to-an-issue   )


For all contributions, a CLA (Contributor License Agreement) needs to be signed [here](https://cla-assistant.io/testsigmahq/testsigma-add-ons) before (or after) the pull request has been submitted. A bot will prompt contributors to sign the CLA via a pull request comment, if necessary.
 
###  Commit messages

- The first line should be a summary of the changes, not exceeding 50
  characters, followed by an optional body that has more details about the
  changes. Refer to [this link](https://github.com/erlang/otp/wiki/writing-good-commit-messages)
  for more information on writing good commit messages.

- Don't add a period/dot (.) at the end of the summary line.
 
##Publish add-ons
Once the pull request has been approved, Project maintainers will publish new add-ons to the marketplace (https://testsigma.com/add-ons). This can take up to 7 days. 
