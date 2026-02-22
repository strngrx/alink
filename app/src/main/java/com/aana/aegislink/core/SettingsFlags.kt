package com.aana.aegislink.core

import com.aana.aegislink.db.SettingsDao
import com.aana.aegislink.utils.SettingsKeys

class DbInstantTrustFlag(
    private val settingsDao: SettingsDao
) : InstantTrustFlag {
    override suspend fun isEnabled(): Boolean {
        return settingsDao.getValue(SettingsKeys.INSTANT_TRUST)?.toBooleanStrictOrNull() ?: false
    }
}

class DbReferralMarketingFlag(
    private val settingsDao: SettingsDao
) : ReferralMarketingFlag {
    override suspend fun isAllowed(): Boolean {
        return settingsDao.getValue(SettingsKeys.REFERRAL_MARKETING)?.toBooleanStrictOrNull() ?: false
    }
}
