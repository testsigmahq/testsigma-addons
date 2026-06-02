package com.testsigma.addons.Utils;

import com.mongodb.client.MongoDatabase;
import com.testsigma.sdk.Logger;
import com.testsigma.sdk.Result;

public interface MongoOperation {
    Result execute(MongoDatabase database, String queryInput, StringBuffer sb, Logger logger);
}
