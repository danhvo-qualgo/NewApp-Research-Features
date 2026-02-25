module Fastlane
  module Actions
    class ActPublishSnapshotAction < Action
      def self.run(params)
        remote = params[:remote]
        gradle_properties_path = "gradle.properties"

        # Read current gradle.properties
        unless File.exist?(gradle_properties_path)
          UI.user_error!("gradle.properties file not found!")
        end

        properties_content = File.read(gradle_properties_path)

        # Get current branch name
        branch_name = sh("git rev-parse --abbrev-ref HEAD").strip
        # Replace / with -
        branch_suffix = branch_name.gsub('/', '-')

        # Get current timestamp
        now = Time.now
        date_suffix = now.strftime("%d-%m-%Y")
        time_suffix = now.strftime("%H-%M-%S")

        # Get current version and auto bump
        current_version_match = properties_content.match(/^VERSION=(.+)$/m)
        if current_version_match
          current_version = current_version_match[1].strip
          # Remove any existing suffix after the version numbers
          base_version = current_version.split('-')[0]
          version_parts = base_version.split('.')

          # Increment the last number
          last_part = version_parts.last
          if last_part.match(/^\d+$/)
            last_part = (last_part.to_i + 1).to_s
          else
            UI.user_error!("Last version part '#{last_part}' is not a number!")
          end

          version_parts[-1] = last_part
          base_version = version_parts.join('.')

          UI.message("Auto-bumped base version from #{current_version} to #{base_version}")
        else
          UI.user_error!("VERSION not found in gradle.properties!")
        end

        # Create snapshot version with suffixes
        snapshot_version = "#{base_version}-#{branch_suffix}-#{date_suffix}-#{time_suffix}-SNAPSHOT"

        # Update VERSION in gradle.properties
        new_content = properties_content.gsub(/^VERSION=.+$/m, "VERSION=#{snapshot_version}")
        File.write(gradle_properties_path, new_content)

        UI.success("Updated VERSION to #{snapshot_version} in gradle.properties")

        # Clean before publishing
        UI.message("Cleaning project...")
        sh("./gradlew clean")

        # Choose publish command based on remote parameter
        if remote
          UI.message("Publishing snapshot to remote repository...")
          gradle_command = "./gradlew publishReleasePublicationToNexusRepository"
        else
          UI.message("Publishing snapshot to local Maven repository...")
          gradle_command = "./gradlew publishToMavenLocal"
        end

        # Execute gradle publish command
        sh(gradle_command)

        UI.success("Successfully published snapshot version #{snapshot_version}!")

        # Print the published version
        puts "Published snapshot version: #{snapshot_version}"

        # Return the version for potential use in other actions
        snapshot_version
      end

      def self.description
        "Publish snapshot version with branch name, date, time (HH-MM-SS) and -SNAPSHOT suffix for testing"
      end

      def self.authors
        ["Core Team"]
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :remote,
            description: "Whether to publish to remote repository (true) or local Maven (false)",
            optional: true,
            default_value: true,
            type: Boolean
          )
        ]
      end

      def self.is_supported?(platform)
        true
      end

      def self.example_code
        [
          'publish_snapshot_version',
          'publish_snapshot_version(remote: false)',
          'publish_snapshot_version(remote: true)'
        ]
      end

      def self.category
        :building
      end
    end
  end
end