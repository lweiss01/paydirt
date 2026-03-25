package com.lweiss01.paydirt.data.local.mapper

import com.lweiss01.paydirt.data.local.entity.CardEntity
import com.lweiss01.paydirt.data.local.entity.PaymentEntity
import com.lweiss01.paydirt.domain.model.AprSource
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.domain.model.Payment

fun CardEntity.toDomain() = Card(
    id = id,
    name = name,
    currentBalance = currentBalance,
    originalBalance = originalBalance,
    apr = apr,
    minPayment = minPayment,
    colorTag = colorTag,
    createdAt = createdAt,
    isArchived = isArchived,
    plaidAccountId = plaidAccountId,
    plaidItemId = plaidItemId,
    plaidInstitutionName = plaidInstitutionName,
    plaidLastRefreshed = plaidLastRefreshed,
    plaidNeedsReauth = plaidNeedsReauth,
    aprSource = AprSource.fromKey(aprSource),
    statementBalance = statementBalance,
    nextPaymentDueDate = nextPaymentDueDate,
    dueDate = dueDate,
)

fun Card.toEntity() = CardEntity(
    id = id,
    name = name,
    currentBalance = currentBalance,
    originalBalance = originalBalance,
    apr = apr,
    minPayment = minPayment,
    colorTag = colorTag,
    createdAt = createdAt,
    isArchived = isArchived,
    plaidAccountId = plaidAccountId,
    plaidItemId = plaidItemId,
    plaidInstitutionName = plaidInstitutionName,
    plaidLastRefreshed = plaidLastRefreshed,
    plaidNeedsReauth = plaidNeedsReauth,
    aprSource = aprSource.key,
    statementBalance = statementBalance,
    nextPaymentDueDate = nextPaymentDueDate,
    dueDate = dueDate,
)

fun PaymentEntity.toDomain() = Payment(
    id = id,
    cardId = cardId,
    amount = amount,
    isExtraPayment = isExtraPayment,
    note = note,
    paidAt = paidAt
)

fun Payment.toEntity() = PaymentEntity(
    id = id,
    cardId = cardId,
    amount = amount,
    isExtraPayment = isExtraPayment,
    note = note,
    paidAt = paidAt
)
