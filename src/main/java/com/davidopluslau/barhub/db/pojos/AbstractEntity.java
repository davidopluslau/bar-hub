package com.davidopluslau.barhub.db.pojos;

import org.jooq.DSLContext;

/**
 * The Report Abstract Entity.
 */
public abstract class AbstractEntity implements Entity {

  /**
   * Persist current state to database given provided DSLContext.
   *
   * @param dsl the DSLContext
   */
  public Boolean save(final DSLContext dsl) {
    return save(dsl, false);
  }

  /**
   * Persist current state to database given provided DSLContext.
   *
   * @param dsl  the DSLContext
   * @param deep whether or not to update relations
   */
  public Boolean save(final DSLContext dsl, final Boolean deep) {
    throw new RuntimeException("save method not Implemented");
  }
}
