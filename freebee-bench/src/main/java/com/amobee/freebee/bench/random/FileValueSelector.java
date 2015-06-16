package com.amobee.freebee.bench.random;

import com.amobee.freebee.bench.DataValueProvider;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileValueSelector implements DataValueProvider.RandomValueSelector
{

    private static final Logger logger = LoggerFactory.getLogger(FileValueSelector.class);

    private static final String[] DEFAULT_VALUE_FILE_EXTENSIONS = {".txt"};
    private static final String[] DEFAULT_VALUE_FILE_SEARCH_LOCATIONS = {"classpath:data/types/"};

    private final List<String> valueFileExtensions;
    private final List<String> valueFileSearchLocations;

    private final Set<String> values;
    private List<String> shuffledValues;
    private int currentIndex;

    private final String dataTypeName;

    public FileValueSelector(final String dataTypeName)
    {
        this.valueFileExtensions = Arrays.asList(DEFAULT_VALUE_FILE_EXTENSIONS);
        this.valueFileSearchLocations = Arrays.asList(DEFAULT_VALUE_FILE_SEARCH_LOCATIONS);

        this.dataTypeName = dataTypeName;
        this.values = loadValues(dataTypeName);
        this.shuffledValues = new ArrayList<>(this.values);
        reshuffleValues();
    }

    @Override
    public String[] getValueArray(final long count)
    {
        if (count > getMaxUniqueValues())
        {
            throw new IllegalArgumentException(String.format("Requested value size greater than max number of unique values for this data provider. " +
                    "requestedSize=%d, maxSize=%d, dataProvider=%s", count, getMaxUniqueValues(), this.dataTypeName));
        }
        if (getRemainingShuffledValues() < count)
        {
            reshuffleValues();
        }
        return getValueStream().limit(count).toArray(String[]::new);
    }

    @Override
    public List<String> getValueList(final long count)
    {
        if (getRemainingShuffledValues() < count)
        {
            reshuffleValues();
        }
        return getValueStream().limit(count).collect(Collectors.toList());
    }

    @Override
    public String getValue()
    {
        if (this.currentIndex >= this.shuffledValues.size())
        {
            reshuffleValues();
        }
        return this.shuffledValues.get(this.currentIndex++);
    }

    @Override
    public int getMaxUniqueValues()
    {
        return this.values.size();
    }

    private int getRemainingShuffledValues()
    {
        return getMaxUniqueValues() - this.currentIndex;
    }

    private void reshuffleValues()
    {
        Utils.shuffleList(this.shuffledValues);
        this.currentIndex = 0;
    }

    private Set<String> loadValues(final String dataTypeName)
    {
        final File valuesFile = findValuesFile(dataTypeName)
                .orElseThrow(() -> new RuntimeException("Could not locate values file for " + dataTypeName));
        final Set<String> loadedValues = loadValues(valuesFile);
        if (loadedValues == null || loadedValues.isEmpty())
        {
            throw new RuntimeException("Values file is empty for " + dataTypeName);
        }
        return loadedValues;
    }

    private Set<String> loadValues(final File valueFile)
    {
        Scanner s = null;
        try
        {
            s = new Scanner(valueFile);
            final Set<String> loadedValues = new HashSet<>();
            final int i = 0;
            while (s.hasNext())
            {
                loadedValues.add(s.next());
            }
            return loadedValues;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not load values from " + valueFile, e);
        } finally
        {
            if (s != null)
            {
                s.close();
            }
        }
    }

    private Optional<File> findValuesFile(final String dataTypeName)
    {
        try
        {

            for (final String searchLocation : this.valueFileSearchLocations)
            {
                if (searchLocation.startsWith("classpath:"))
                {
                    logger.debug("Searching {} for {} values file.", searchLocation, dataTypeName);

                    String classpathLocation = searchLocation.substring(searchLocation.indexOf(':') + 1, searchLocation.length());
                    if (!classpathLocation.endsWith("/"))
                    {
                        classpathLocation = classpathLocation + "/";
                    }

                    for (final String fileExtension : this.valueFileExtensions)
                    {
                        final String valueFileLocation = classpathLocation + dataTypeName + fileExtension;
                        final URL classpathResourceUrl = getClass().getClassLoader().getResource(valueFileLocation);
                        if (classpathResourceUrl != null && classpathResourceUrl.getFile() != null)
                        {
                            final File valueFile = new File(classpathResourceUrl.getFile());
                            if (valueFile.exists())
                            {
                                logger.debug("Found {} values file at {}", dataTypeName, valueFileLocation);
                                return Optional.of(valueFile);
                            }
                        }
                    }
                }
                else
                {
                    // TODO implement arbitrary file location loading
                    logger.warn("Configured data value file search location is '{}', " +
                            "but currently on classpath locations are supported. " +
                            "Skipping without searching this location.", searchLocation);
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to locate values file due to: " + e.getLocalizedMessage(), e);
        }
        return Optional.empty();
    }

}
