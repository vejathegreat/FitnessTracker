package com.velaphi.core

import org.junit.runner.RunWith
import org.junit.runners.Suite
import com.velaphi.core.data.FoodRepositoryTest

@RunWith(Suite::class)
@Suite.SuiteClasses(
    FoodRepositoryTest::class
)
class CoreTestSuite {
    // This class serves as a test suite for the core module
}
