/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ajoberstar.reckon.gradle;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.reckon.core.NormalStrategy;
import org.ajoberstar.reckon.core.PreReleaseStrategy;
import org.ajoberstar.reckon.core.Reckoner;
import org.ajoberstar.reckon.core.VcsInventorySupplier;
import org.ajoberstar.reckon.core.git.GitInventorySupplier;
import org.ajoberstar.reckon.core.strategy.ScopeNormalStrategy;
import org.ajoberstar.reckon.core.strategy.StagePreReleaseStrategy;
import org.ajoberstar.reckon.core.strategy.SnapshotPreReleaseStrategy;
import org.gradle.api.Project;

public class ReckonExtension {
  private static final String SCOPE_PROP = "reckon.scope";
  private static final String STAGE_PROP = "reckon.stage";
  private static final String SNAPSHOT_PROP = "reckon.snapshot";

  private Project project;
  private VcsInventorySupplier vcsInventory;
  private NormalStrategy normal;
  private PreReleaseStrategy preRelease;

  public ReckonExtension(Project project) {
    this.project = project;
  }

  public void setVcsInventory(VcsInventorySupplier vcsInventory) {
    this.vcsInventory = vcsInventory;
  }

  public void setNormal(NormalStrategy normal) {
    this.normal = normal;
  }

  public void setPreRelease(PreReleaseStrategy preRelease) {
    this.preRelease = preRelease;
  }

  public VcsInventorySupplier git(Grgit grgit) {
    return new GitInventorySupplier(grgit.getRepository().getJgit().getRepository());
  }

  public NormalStrategy scopeFromProp() {
    Supplier<Optional<String>> supplier =
        () -> Optional.ofNullable(project.findProperty(SCOPE_PROP)).map(Object::toString);
    return new ScopeNormalStrategy(supplier);
  }

  public PreReleaseStrategy stageFromProp(String... stages) {
    Set<String> stageSet = Arrays.stream(stages).collect(Collectors.toSet());
    Supplier<Optional<String>> supplier =
        () -> Optional.ofNullable(project.findProperty(STAGE_PROP)).map(Object::toString);
    return new StagePreReleaseStrategy(stageSet, supplier);
  }

  public PreReleaseStrategy snapshotFromProp() {
    Supplier<Boolean> supplier =
        () ->
            Optional.ofNullable(project.findProperty(SNAPSHOT_PROP))
                .map(Object::toString)
                .map(Boolean::parseBoolean)
                .orElse(true);
    return new SnapshotPreReleaseStrategy(supplier);
  }

  String reckonVersion() {
    return Reckoner.reckon(vcsInventory, normal, preRelease);
  }
}