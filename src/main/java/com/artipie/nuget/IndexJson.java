/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/nuget-adapter/artipie/LICENSE.txt
 */
package com.artipie.nuget;

import com.artipie.nuget.metadata.CatalogEntry;
import com.artipie.nuget.metadata.Nuspec;
import com.artipie.nuget.metadata.PackageId;
import com.vdurmont.semver4j.Semver;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * Index json operations. Index json is metadata file for various version of NuGet package, it's
 * called registration page in the repository docs.
 * <a href="https://learn.microsoft.com/en-us/nuget/api/registration-base-url-resource#registration-page-object">Registration page</a>.
 * @since 1.5
 */
public interface IndexJson {

    /**
     * Delete nuget package by name and version from index.json.
     * @since 1.6
     */
    final class Delete {

        /**
         * Input stream with existing index json metadata.
         */
        private final InputStream input;

        /**
         * Ctor.
         * @param input Input stream with existing index json metadata
         */
        public Delete(final InputStream input) {
            this.input = input;
        }

        /**
         * Perform delete operation.
         * @param name The name of the package
         * @param version Package version
         * @return Json object with the index without removed package
         */
        public JsonObject perform(final String name, final String version) {
            final JsonObjectBuilder res = Json.createObjectBuilder();
            final JsonObject old = Json.createReader(this.input).readObject();
            List<JsonObject> items = Collections.emptyList();
            if (old.containsKey(Update.ITEMS) && !old.getJsonArray(Update.ITEMS).isEmpty()) {
                final JsonObject obj = old.getJsonArray(Update.ITEMS).get(0).asJsonObject();
                if (obj.containsKey(Update.ITEMS) && !obj.getJsonArray(Update.ITEMS).isEmpty()) {
                    final JsonArray arr = obj.getJsonArray(Update.ITEMS);
                    items = arr.stream().map(JsonValue::asJsonObject).filter(
                        item -> {
                            final JsonObject entry = item.getJsonObject(Update.CATALOG_ENTRY);
                            return !(entry.getString("id").equals(new PackageId(name).normalized())
                                && new Semver(Update.version(item)).equals(new Semver(version)));
                        }
                    ).sorted(Comparator.comparing(val -> new Semver(Update.version(val))))
                        .collect(Collectors.toList());
                }
            }
            if (!items.isEmpty()) {
                res.add(Update.LOWER, Update.version(items.get(0)));
                res.add(Update.UPPER, Update.version(items.get(items.size() - 1)));
                Update.addIdAndCount(res, Update.NULL, items.size());
                final JsonArrayBuilder builder = Json.createArrayBuilder();
                items.forEach(builder::add);
                res.add(Update.ITEMS, builder);
            }
            return Json.createObjectBuilder().add(Update.COUNT, 1)
                .add(Update.ITEMS, Json.createArrayBuilder().add(res)).build();
        }
    }

    /**
     * Update (or create) index.json metadata by adding
     * package info from NUSPEC package metadata.
     * @since 1.5
     */
    final class Update {

        /**
         * Default null value for index.json required fields with urls values.
         */
        private static final String NULL = "null";

        /**
         * The name of the `@id` json field.
         */
        private static final String ID = "@id";

        /**
         * The name of the `items` json field.
         */
        private static final String ITEMS = "items";

        /**
         * The name of the `catalogEntry` json field.
         */
        private static final String CATALOG_ENTRY = "catalogEntry";

        /**
         * The name of the `count` json field.
         */
        private static final String COUNT = "count";

        /**
         * The name of the `lower` json field.
         */
        private static final String LOWER = "lower";

        /**
         * The name of the `upper` json field.
         */
        private static final String UPPER = "upper";

        /**
         * Optional input stream with existing index json metadata.
         */
        private final Optional<InputStream> input;

        /**
         * Primary ctor.
         * @param input Optional input stream with existing index json metadata
         */
        public Update(final Optional<InputStream> input) {
            this.input = input;
        }

        /**
         * Ctor with empty optional for input stream.
         */
        public Update() {
            this(Optional.empty());
        }

        /**
         * Ctor.
         * @param input Optional input stream with existing index json metadata
         */
        public Update(final InputStream input) {
            this(Optional.of(input));
        }

        /**
         * Creates or updates index.json by adding information about new provided package. If such
         * package version already exists in index.json, package metadata are replaced.
         * {@link  NuGetPackage} instance can be created from NuGet package input stream by calling
         * constructor {@link Nupkg#Nupkg(InputStream)}.
         * In the resulting json object catalogEntries are placed in the ascending order by
         * package version. Required url fields (like @id, packageContent, @id of the catalogEntry)
         * are set to "null" string.
         * @param pkg New package to add
         * @return Updated index.json metadata as {@link JsonObject}
         */
        public JsonObject perform(final NuGetPackage pkg) {
            final JsonObjectBuilder res = Json.createObjectBuilder();
            final Nuspec nuspec = pkg.nuspec();
            final JsonObject newest = newPackageJsonItem(nuspec);
            final String version = nuspec.version().normalized();
            final JsonArrayBuilder itemsbuilder = Json.createArrayBuilder();
            if (this.input.isPresent()) {
                final JsonObject old = Json.createReader(this.input.get()).readObject();
                final List<JsonObject> list = sortedPackages(newest, version, old);
                list.forEach(itemsbuilder::add);
                addIdAndCount(res, old.getString(Update.ID, Update.NULL), list.size());
            } else {
                itemsbuilder.add(newest);
                addIdAndCount(res, Update.NULL, 1);
            }
            final JsonArray items = itemsbuilder.build();
            res.add(Update.UPPER, version(items.get(items.size() - 1).asJsonObject()));
            res.add(Update.LOWER, version(items.get(0).asJsonObject()));
            res.add(Update.ITEMS, items);
            return Json.createObjectBuilder().add(Update.COUNT, 1)
                .add(Update.ITEMS, Json.createArrayBuilder().add(res)).build();
        }

        /**
         * Here we check if the existing packages metadata array is present and not empty and if it
         * already contains metadata for new package version, replace it if it does or simply add
         * new package meta if it does not, then we sort packages meta by package version
         * and return sorted list.
         * @param newest New package metadata in json format
         * @param version Version of new package
         * @param old Existing packages metadata array
         * @return Sorted by packages version list of the packages metadata including new package
         * @checkstyle InnerAssignmentCheck (10 lines)
         */
        @SuppressWarnings("PMD.AssignmentInOperand")
        private static List<JsonObject> sortedPackages(final JsonObject newest,
            final String version, final JsonObject old) {
            List<JsonObject> list = Collections.singletonList(newest);
            if (old.containsKey(Update.ITEMS) && !old.getJsonArray(Update.ITEMS).isEmpty()) {
                final JsonObject value = old.getJsonArray(Update.ITEMS).get(0).asJsonObject();
                final JsonArray arr;
                if (value.containsKey(Update.ITEMS)
                    && !(arr = value.getJsonArray(Update.ITEMS)).isEmpty()) {
                    list = new ArrayList<>(arr.size() + 1);
                    arr.stream().map(JsonValue::asJsonObject)
                        .filter(val -> !version.equals(version(val))).forEach(list::add);
                    list.add(newest);
                    list.sort(Comparator.comparing(val -> new Semver(version(val))));
                }
            }
            return list;
        }

        /**
         * Build json item for new package.
         * @param nuspec New package to add
         * @return Json object of the new package
         */
        private static JsonObject newPackageJsonItem(final Nuspec nuspec) {
            return Json.createObjectBuilder()
                .add(Update.ID, Update.NULL).add("packageContent", Update.NULL)
                .add(Update.CATALOG_ENTRY, new CatalogEntry.FromNuspec(nuspec).asJson()).build();
        }

        /**
         * Add `@id` and `count` fields into resulting json object builder.
         * @param res Resulting json object builder
         * @param id Field value `@id`
         * @param cnt Count field value
         */
        private static void addIdAndCount(final JsonObjectBuilder res, final String id,
            final int cnt) {
            res.add(Update.ID, id);
            res.add(Update.COUNT, cnt);
        }

        /**
         * Obtain package version from json value item.
         * @param val Json Value item
         * @return String version
         */
        private static String version(final JsonObject val) {
            return val.getJsonObject(Update.CATALOG_ENTRY).getString("version");
        }
    }

}
